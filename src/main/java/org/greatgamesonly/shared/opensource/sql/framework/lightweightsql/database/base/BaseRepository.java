package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbEntityColumnToFieldToGetter;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.Entity;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.Repository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.RepositoryException;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.errors.RepositoryError;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.greatgamesonly.reflection.utils.ReflectionUtils.*;
import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.*;

public abstract class BaseRepository<E extends BaseEntity> {

    private static Connection connection;
    private Class<E> dbEntityClass;
    private Class<E[]> dbEntityArrayClass;

    public BaseRepository() {}

    public Class<E> getDbEntityClass() {
        if(dbEntityClass == null) {
            if(this.getClass().isAnnotationPresent(Repository.class)) {
                dbEntityClass = (Class<E>) this.getClass().getAnnotation(Repository.class).dbEntityClass();
            } else if(this.getClass().getName().endsWith("_Subclass")/*Quarkus_Support*/) {
                dbEntityClass = (Class<E>) getClassByName(this.getClass().getName().replaceAll("_Subclass","")).getAnnotation(Repository.class).dbEntityClass();
            }
        }
        return dbEntityClass;
    }

    public Class<E[]> getDbEntityArrayClass() {
        if(dbEntityArrayClass == null) {
            dbEntityArrayClass = (Class<E[]>) Array.newInstance(getDbEntityClass(),0).getClass();
        }
        return dbEntityArrayClass;
    }

    public Map<String, String> getDbConnectionDetails() {
        return DbConnectionManager.CONNECTION_DETAILS;
    }

    public E getById(Long id) throws RepositoryException {
        List<E> entitiesRetrieved = executeGetQuery("SELECT * FROM " + getDbEntityClass().getAnnotation(Entity.class).tableName() + " WHERE " + getPrimaryKeyDbColumnName(getDbEntityClass()) + " = " + id);
        return (entitiesRetrieved != null && !entitiesRetrieved.isEmpty()) ? entitiesRetrieved.get(0) : null;
    }

    public List<E> getAll() throws RepositoryException {
        return executeGetQuery("SELECT * FROM " + getDbEntityClass().getAnnotation(Entity.class).tableName());
    }

    public List<E> getAllByMinAndMaxAndColumnName(Object minId, Object maxId, String columnName) throws RepositoryException {
        return getAllByMinAndMaxAndColumnName(minId, maxId, columnName, null);
    }

    public Map<Long,E> getAllByMinAndMaxAndColumnNameAsMap(Object minId, Object maxId, String columnName) throws RepositoryException {
        return getAllByMinAndMaxAndColumnName(minId, maxId, columnName, null).stream().collect(Collectors.toMap(BaseEntity::getId, entity -> entity));
    }

    public List<E> getAllByMinAndMaxAndColumnName(Object minId, Object maxId, String columnName, String additionalWhereQuery) throws RepositoryException {
        return executeGetQuery("SELECT * FROM " + getDbEntityClass().getAnnotation(Entity.class).tableName() +
                " WHERE " + columnName + " >= " + returnPreparedValueForQuery(minId) +
                " AND " + columnName + " <= " + returnPreparedValueForQuery(maxId) +
                ((additionalWhereQuery != null && !additionalWhereQuery.isBlank()) ? " AND " + additionalWhereQuery : ""));
    }

    public void deleteById(Long id) throws RepositoryException {
        executeDeleteQuery("DELETE FROM " + getDbEntityClass().getAnnotation(Entity.class).tableName() + " WHERE " + getPrimaryKeyDbColumnName(getDbEntityClass()) + " = " + id);
    }

    public E getByColumnName(String columnName, Object columnValue) throws RepositoryException {
        List<E> entitiesRetrieved = executeGetQuery("SELECT * FROM " +
                getDbEntityClass().getAnnotation(Entity.class).tableName() + " WHERE " + columnName + " = " +
                returnPreparedValueForQuery(columnValue));
        return (entitiesRetrieved != null && !entitiesRetrieved.isEmpty()) ? entitiesRetrieved.get(0) : null;
    }

    public E getByColumnNameOrderByPrimaryKey(String columnName, Object columnValue, OrderBy descOrAsc) throws RepositoryException {
        List<E> entitiesRetrieved = executeGetQuery("SELECT * FROM " +
                getDbEntityClass().getAnnotation(Entity.class).tableName() + " WHERE " + columnName + " = " +
                returnPreparedValueForQuery(columnValue) +
                descOrAsc.getQueryEquivalent(getPrimaryKeyDbColumnName(getDbEntityClass())));
        return (entitiesRetrieved != null && !entitiesRetrieved.isEmpty()) ? entitiesRetrieved.get(0) : null;
    }
    public E getByColumnNameOrderByColumn(String columnName, Object columnValue, String orderByColumn, OrderBy descOrAsc) throws RepositoryException {
        List<E> entitiesRetrieved = executeGetQuery("SELECT * FROM " +
                getDbEntityClass().getAnnotation(Entity.class).tableName() + " WHERE " + columnName + " = " +
                returnPreparedValueForQuery(columnValue) +
                descOrAsc.getQueryEquivalent(orderByColumn));
        return (entitiesRetrieved != null && !entitiesRetrieved.isEmpty()) ? entitiesRetrieved.get(0) : null;
    }

    public Long countByColumn(String columnName, Object columnKey) throws RepositoryException {
        try {
            long countTotal;
            ResultSet resultSet = executeQueryRaw("SELECT COUNT(*) as total FROM " +
                    getDbEntityClass().getAnnotation(Entity.class).tableName() + " WHERE " + columnName + " = " +
                    returnPreparedValueForQuery(columnKey) + ";");
            resultSet.next();
            countTotal = resultSet.getLong("total");
            resultSet.close();
            return countTotal;
        } catch (SQLException e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_COUNT_BY_FIELD__ERROR,e);
        }
    }

    public Long countByColumns(String columnName, Object columnKey, String columnName2, Object columnKey2) throws RepositoryException {
        try {
            long countTotal;
            ResultSet resultSet = executeQueryRaw("SELECT COUNT(*) AS total FROM " +
                    getDbEntityClass().getAnnotation(Entity.class).tableName() + " WHERE " + columnName + " = " +
                    returnPreparedValueForQuery(columnKey) +
                    " AND " + columnName2 + " = " + returnPreparedValueForQuery(columnKey2) + ";");
            resultSet.next();
            countTotal = resultSet.getLong("total");
            resultSet.close();
            return countTotal;
        } catch (SQLException e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_COUNT_BY_FIELD__ERROR,e);
        }
    }

    public E insertOrUpdate(E entity) throws RepositoryException {
        E existingEntity = entity.getId() != null ? getById(entity.getId()) : null;
        if(existingEntity == null) {
            existingEntity = insertEntities(entity).get(0);
        } else {
            Collection<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters;
            try {
                dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());
            } catch (IntrospectionException e) {
                throw new RepositoryException(RepositoryError.REPOSITORY_UPDATE_ENTITY_WITH_ENTITY__ERROR,e);
            }
            E finalExistingEntity = existingEntity;

            for(DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : dbEntityColumnToFieldToGetters) {
                if(dbEntityColumnToFieldToGetter.canBeUpdatedInDb() && !dbEntityColumnToFieldToGetter.isPrimaryKey()) {
                    try {
                        callReflectionMethodQuick(
                                finalExistingEntity,
                                dbEntityColumnToFieldToGetter.getSetterMethodName(),
                                new Object[]{callReflectionMethodQuick(entity, dbEntityColumnToFieldToGetter.getGetterMethodName())},
                                dbEntityColumnToFieldToGetter.getMethodParamTypes()
                        );
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new RepositoryException(RepositoryError.REPOSITORY_CALL_REFLECTION_METHOD__ERROR, e);
                    }
                }
            }
            existingEntity = updateEntities(finalExistingEntity).get(0);
        }
        return existingEntity;
    }

    public List<E> insertOrUpdateEntities(List<E> entities) throws RepositoryException {
        if(entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        Object entitiesToInsert = Array.newInstance(getDbEntityClass(),entities.size());
        Object entitiesToUpdate = Array.newInstance(getDbEntityClass(),entities.size());
        entities = entities.stream().sorted(Comparator.comparingLong(BaseEntity::getId)).collect(Collectors.toList());
        Map<Long,E> existingEntities = getAllByMinAndMaxAndColumnNameAsMap(entities.get(0).getId(),entities.get(entities.size()-1).getId(),"id");
        Collection<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters;
        try {
            dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());
        } catch (IntrospectionException e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_UPDATE_ENTITY_WITH_ENTITY__ERROR, e);
        }
        for(int i = 0; i < entities.size(); i++) {
            E existingEntity = entities.get(i).getId() != null ? existingEntities.get(entities.get(i).getId()) : null;
            if (existingEntity == null) {
                Array.set(entitiesToInsert, i, entities.get(i));
            } else {
                for (DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : dbEntityColumnToFieldToGetters) {
                    if (dbEntityColumnToFieldToGetter.canBeUpdatedInDb() && !dbEntityColumnToFieldToGetter.isPrimaryKey()) {
                        try {
                            callReflectionMethodQuick(
                                    existingEntity,
                                    dbEntityColumnToFieldToGetter.getSetterMethodName(),
                                    new Object[]{callReflectionMethodQuick(entities.get(i), dbEntityColumnToFieldToGetter.getGetterMethodName())},
                                    dbEntityColumnToFieldToGetter.getMethodParamTypes()
                            );
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            throw new RepositoryException(RepositoryError.REPOSITORY_CALL_REFLECTION_METHOD__ERROR, e);
                        }
                    }
                }
                Array.set(entitiesToUpdate, i, existingEntity);
            }
        }
        return Stream.concat(insertEntities(getDbEntityArrayClass().cast(entitiesToInsert)).stream(),updateEntities(getDbEntityArrayClass().cast(entitiesToUpdate)).stream()).collect(Collectors.toList());
    }

    protected List<E> executeGetQuery(String queryToRun, Object... queryParameters) throws RepositoryException {
        return executeQuery(queryToRun, QueryType.GET, null, queryParameters);
    }

    protected List<E> executeInsertQuery(String queryToRun, List<DbEntityColumnToFieldToGetter> relationFieldToGetters, Object... queryParameters) throws RepositoryException {
        return executeQuery(queryToRun, QueryType.INSERT, relationFieldToGetters, queryParameters);
    }

    protected List<E> executeUpdateQuery(String queryToRun, List<DbEntityColumnToFieldToGetter> relationFieldToGetters, Object... queryParameters) throws RepositoryException {
        return executeQuery(queryToRun, QueryType.INSERT, relationFieldToGetters, queryParameters);
    }

    protected void executeDeleteQuery(String queryToRun, Object... queryParameters) throws RepositoryException {
        executeQuery(queryToRun, QueryType.DELETE, null,queryParameters);
    }

    private List<E> executeQuery(String queryToRun, QueryType queryType, List<DbEntityColumnToFieldToGetter> relationFieldToGetters, Object... queryParameters) throws RepositoryException {
        List<E> entityList = new ArrayList<>();
        try {
            if(queryType.equals(QueryType.INSERT)) {
                    entityList = getRunner().insert(getConnection(), queryToRun, getQueryResultHandler(), queryParameters);
            } else if(queryType.equals(QueryType.UPDATE)) {
                    entityList = getRunner().execute(getConnection(), queryToRun, getQueryResultHandler()).stream().flatMap(List::stream).collect(Collectors.toList());
            } else if(queryType.equals(QueryType.DELETE)) {
                getRunner().execute(getConnection(), queryToRun, getQueryResultHandler());
            } else if(queryType.equals(QueryType.GET)) {
                entityList = getRunner().query(getConnection(), queryToRun, getQueryResultHandler(), queryParameters);
            }
            if(queryType.equals(QueryType.GET)) {
                if(relationFieldToGetters == null) {
                    relationFieldToGetters = getAllRelationFieldToGetters(getDbEntityClass());
                }
                if(!relationFieldToGetters.isEmpty()) {
                    // Optimize relation get queries to be quicker - BEGIN
                    HashMap<Long, E> entityHashMap = new HashMap<>();
                    Long minId = 0L;
                    Long maxId = 0L;
                    for (E entity : entityList) {
                        if (minId == null) {
                            minId = entity.getId();
                        }
                        if (maxId == null) {
                            maxId = entity.getId();
                        }
                        if (entity.getId() < minId) {
                            minId = entity.getId();
                        }
                        if (entity.getId() > maxId) {
                            maxId = entity.getId();
                        }
                        entityHashMap.put(entity.getId(), entity);
                    }
                    // Optimize relation get queries to be quicker - END
                    for (DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : relationFieldToGetters) {
                        if (dbEntityColumnToFieldToGetter.isForManyToOneRelation()) {
                            continue; // Handled in the BaseBeanListHandler
                        }
                        BaseRepository<? extends BaseEntity> relationEntityRepo = dbEntityColumnToFieldToGetter.getLinkedClassEntity().getAnnotation(Entity.class).repositoryClass().getDeclaredConstructor().newInstance();
                        List<? extends BaseEntity> toRelationEntities = relationEntityRepo.getAllByMinAndMaxAndColumnName(minId, maxId, dbEntityColumnToFieldToGetter.getReferenceToColumnName(), dbEntityColumnToFieldToGetter.getAdditionalQueryToAdd());
                        for (BaseEntity relationEntity : toRelationEntities) {
                            E entityToSetToManyRelationsOn = entityHashMap.get((Long) callReflectionMethodQuick(relationEntity, dbEntityColumnToFieldToGetter.getReferenceToColumnClassFieldGetterMethodName()));
                            callReflectionMethodQuick(entityToSetToManyRelationsOn, dbEntityColumnToFieldToGetter.getSetterMethodName(), new Object[]{relationEntity}, dbEntityColumnToFieldToGetter.getMethodParamTypes());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            if(e.getSQLState() != null && e.getSQLState().startsWith("23505")) {
                throw new RepositoryException(RepositoryError.REPOSITORY_INSERT_CONSTRAINT_VIOLATION_ERROR, e.getMessage(), e);
            }
            throw new RepositoryException(RepositoryError.REPOSITORY_GET__ERROR,  String.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage()), e);
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_GET__ERROR, e.getMessage() + " non sql error", e);
        } finally {
            try {
                DbUtils.close(getConnection());
            } catch (SQLException e) {
                throw new RepositoryException(RepositoryError.REPOSITORY_GET__ERROR, e);
            }
        }
        return entityList;
    }

    protected ResultSet executeQueryRaw(String queryToRun) throws RepositoryException {
        ResultSet entityList = null;
        try {
            CallableStatement callStatement = getConnection().prepareCall(queryToRun);
            if(queryToRun.startsWith("SELECT")) {
                entityList = callStatement.executeQuery();
            } else {
                callStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            throw new RepositoryException(RepositoryError.REPOSITORY_GET__ERROR,  String.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage()), e);
        } catch (Exception e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_GET__ERROR, e.getMessage() + " non sql error", e);
        } finally {
            try {
                DbUtils.close(getConnection());
            } catch (SQLException e) {
                throw new RepositoryException(RepositoryError.REPOSITORY_GET__ERROR, e);
            }
        }
        return entityList;
    }

    public final List<E> insertEntitiesListGeneric(List<? extends BaseEntity> entitiesToInsert) throws RepositoryException {
        if(entitiesToInsert == null || entitiesToInsert.isEmpty()) {
            return new ArrayList<>();
        }
        Object toInsert = Array.newInstance(getDbEntityClass(),entitiesToInsert.size());
        try {
            for (int i = 0; i < entitiesToInsert.size(); i++) {
                Array.set(toInsert,i,entitiesToInsert.get(i));
            }
        } catch (Exception e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_INSERT__ERROR, e.getMessage());
        }
        return insertEntities(getDbEntityArrayClass().cast(toInsert));
    }

    @SafeVarargs
    public final List<E> insertEntities(E... entitiesToInsert) throws RepositoryException {
        StringBuilder stringBuilder = new StringBuilder();
        List<DbEntityColumnToFieldToGetter> relationFieldToGetters;
        try {
            if(entitiesToInsert == null || entitiesToInsert.length <= 0) {
                return new ArrayList<>();
            }
            Collection<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());
            relationFieldToGetters = getAllRelationFieldToGetters(getDbEntityClass());

            // HANDLE ONE-TO-MANY, ONE-TO-ONE, MANY-TO-ONE BEGIN
            handleEntityRelationshipInsertsOrUpdates(relationFieldToGetters,entitiesToInsert);
            // HANDLE ONE-TO-MANY, ONE-TO-ONE, MANY-TO-ONE END

            stringBuilder.append(String.format("INSERT INTO %s (", getDbEntityClass().getAnnotation(Entity.class).tableName()));
            stringBuilder.append(
                dbEntityColumnToFieldToGetters.stream()
                .filter(this::includeDbEntityColumnToFieldToGetterInInsertOrUpdateOperations)
                .map(DbEntityColumnToFieldToGetter::getDbColumnName)
                .collect(Collectors.joining(","))
            );
            stringBuilder.append(") VALUES ");
            int toInsertIterator = 0;
            for (E entityToInsert : entitiesToInsert) {
                if(entityToInsert == null) {
                    continue;
                }
                stringBuilder.append("(");

                List<String> toAppendValues = new ArrayList<>();
                for(DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : dbEntityColumnToFieldToGetters) {
                    try {
                        if(dbEntityColumnToFieldToGetter.hasSetter() && includeDbEntityColumnToFieldToGetterInInsertOrUpdateOperations(dbEntityColumnToFieldToGetter)) {
                            Object getterValue = callReflectionMethodQuick(entityToInsert, dbEntityColumnToFieldToGetter.getGetterMethodName());
                            toAppendValues.add((getterValue != null) ? returnPreparedValueForQuery(getterValue) : null);
                        }
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new RepositoryException(RepositoryError.REPOSITORY_CALL_REFLECTION_METHOD__ERROR,e);
                    }
                }
                stringBuilder.append(String.join(",",toAppendValues));
                stringBuilder.append(")");
                if (!entityToInsert.equals(entitiesToInsert[entitiesToInsert.length - 1])) {
                    stringBuilder.append(",");
                }
                toInsertIterator++;
            }
        } catch (IntrospectionException e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_PREPARE_INSERT__ERROR, e);
        }
        return executeInsertQuery(stringBuilder.toString(), relationFieldToGetters);
    }

    public final List<E> updateEntitiesListGeneric(List<? extends BaseEntity> entitiesToUpdate) throws RepositoryException {
        if(entitiesToUpdate == null || entitiesToUpdate.isEmpty()) {
            return new ArrayList<>();
        }
        Object toUpdate = Array.newInstance(getDbEntityClass(),entitiesToUpdate.size());
        try {
            for (int i = 0; i < entitiesToUpdate.size(); i++) {
                Array.set(toUpdate,i,entitiesToUpdate.get(i));
            }
        } catch (Exception e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_INSERT__ERROR, e.getMessage());
        }
        return updateEntities(getDbEntityArrayClass().cast(toUpdate));
    }

    @SafeVarargs
    public final List<E> updateEntities(E... entitiesToUpdate) throws RepositoryException {
        List<E> result = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        List<DbEntityColumnToFieldToGetter> relationFieldToGetters;
        try {
            if(entitiesToUpdate == null || entitiesToUpdate.length <= 0) {
                return new ArrayList<>();
            }
            Collection<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());
            relationFieldToGetters = getAllRelationFieldToGetters(getDbEntityClass());

            // HANDLE ONE-TO-MANY, ONE-TO-ONE, MANY-TO-ONE BEGIN
            handleEntityRelationshipInsertsOrUpdates(relationFieldToGetters,entitiesToUpdate);
            // HANDLE ONE-TO-MANY, ONE-TO-ONE, MANY-TO-ONE END

            String primaryKeyColumnName = getPrimaryKeyDbColumnName(dbEntityColumnToFieldToGetters);

            stringBuilder.append(String.format("UPDATE %s SET ", getDbEntityClass().getAnnotation(Entity.class).tableName()));
            for (BaseEntity entityToUpdate : entitiesToUpdate) {
                if(entityToUpdate == null) {
                    continue;
                }

                List<String> toAppendValues = new ArrayList<>();
                for(DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : dbEntityColumnToFieldToGetters) {
                    try {
                        if(includeDbEntityColumnToFieldToGetterInInsertOrUpdateOperations(dbEntityColumnToFieldToGetter) && dbEntityColumnToFieldToGetter.canBeUpdatedInDb()) {
                            Object getterValue = callReflectionMethodQuick(entityToUpdate, dbEntityColumnToFieldToGetter.getGetterMethodName());
                            if(getterValue == null && dbEntityColumnToFieldToGetter.isModifyDateAutoSet()) {
                                getterValue = nowDbTimestamp(dbEntityColumnToFieldToGetter.getModifyDateAutoSetTimezone());
                            }
                            toAppendValues.add(dbEntityColumnToFieldToGetter.getDbColumnName() + " = " + ((getterValue != null) ? returnPreparedValueForQuery(getterValue) : null));
                        }
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new RepositoryException(RepositoryError.REPOSITORY_CALL_REFLECTION_METHOD__ERROR,e);
                    }
                }
                stringBuilder.append(String.join(",",toAppendValues));
                if (!entityToUpdate.equals(entitiesToUpdate[entitiesToUpdate.length - 1])) {
                    stringBuilder.append(",");
                } else {
                    stringBuilder.append(String.format(" WHERE %s = %d", primaryKeyColumnName, entityToUpdate.getId()));
                }
                result = executeUpdateQuery(stringBuilder.toString(), relationFieldToGetters);
            }
        } catch (IntrospectionException e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_UPDATE_ENTITY__ERROR, e);
        }
        return result;
    }

    @SafeVarargs
    public final void deleteEntities(E... entitiesToDelete) throws RepositoryException {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if(entitiesToDelete == null || entitiesToDelete.length <= 0) {
                throw new RepositoryException(RepositoryError.REPOSITORY_INSERT__ERROR, "null or empty entitiesToDelete value was passed");
            }
            Collection<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());
            String primaryKeyColumnName = getPrimaryKeyDbColumnName(dbEntityColumnToFieldToGetters);

            stringBuilder.append(String.format("DELETE FROM %s WHERE %s IN ( ", getDbEntityClass().getAnnotation(Entity.class).tableName(), primaryKeyColumnName));
            stringBuilder.append(
                Arrays.stream(entitiesToDelete)
                .map(entity -> entity.getId().toString())
                .collect(Collectors.joining(","))
            );
            stringBuilder.append(" );");
            executeDeleteQuery(stringBuilder.toString());
        } catch (Exception e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_DELETE_ENTITY__ERROR, e);
        }
    }

    @SafeVarargs
    private List<?> handleEntityRelationshipInsertsOrUpdates(List<DbEntityColumnToFieldToGetter> relationFieldToGetters, E... entitiesParam) throws RepositoryException {
        List<?> relationToEntities = new ArrayList<BaseEntity>();
        try {
            // Handle Relational db inserts or updates
            if (!relationFieldToGetters.isEmpty()) {
                for (DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : relationFieldToGetters) {
                    List<? extends BaseEntity> insertEntities;
                    List<? extends BaseEntity> updateEntities;
                    List<? extends BaseEntity> relationToEntitiesInsertedOrUpdated = new ArrayList<>();
                    for (E entity : entitiesParam) {
                        if(entity == null) {
                            continue;
                        }
                        // do not call addAll if the relationship type is for single entity relations and not multiple relations to one relation
                        if (dbEntityColumnToFieldToGetter.isForManyToOneRelation() || dbEntityColumnToFieldToGetter.isForOneToOneRelation()) {
                            relationToEntitiesInsertedOrUpdated.add(callReflectionMethodGeneric(entity, dbEntityColumnToFieldToGetter.getGetterMethodName()));
                        } else {
                            relationToEntitiesInsertedOrUpdated.addAll(callReflectionMethodGeneric(entity, dbEntityColumnToFieldToGetter.getGetterMethodName()));
                        }
                    }
                    if(isEmptyOrBlankCollection(relationToEntitiesInsertedOrUpdated)) {
                        continue;
                    }
                    BaseRepository<?> toEntityRepo = dbEntityColumnToFieldToGetter.getLinkedClassEntity().getAnnotation(Entity.class).repositoryClass().getDeclaredConstructor().newInstance();
                    insertEntities = toEntityRepo.insertEntitiesListGeneric(relationToEntitiesInsertedOrUpdated.stream().filter(toManyEntity -> toManyEntity != null && toManyEntity.getId() == null).collect(Collectors.toList()));
                    updateEntities = toEntityRepo.updateEntitiesListGeneric(relationToEntitiesInsertedOrUpdated.stream().filter(toManyEntity -> toManyEntity != null && toManyEntity.getId() != null).collect(Collectors.toList()));
                    relationToEntitiesInsertedOrUpdated = Stream.concat(insertEntities.stream(), updateEntities.stream()).collect(Collectors.toList());
                    relationToEntities = Stream.concat(relationToEntities.stream(), relationToEntitiesInsertedOrUpdated.stream()).collect(Collectors.toList());
                    for (E entity : entitiesParam) {
                        List<BaseEntity> toAdd = new ArrayList<>();
                        if(dbEntityColumnToFieldToGetter.isForManyToOneRelation()) {
                            DbEntityColumnToFieldToGetter manyToOneRefIdRelationFieldToGetter = getManyToOneRefIdRelationFieldToGetter(entity.getClass(), dbEntityColumnToFieldToGetter);
                            callReflectionMethodQuick(entity, manyToOneRefIdRelationFieldToGetter.getSetterMethodName(), new Object[]{relationToEntitiesInsertedOrUpdated.get(0).getId()}, manyToOneRefIdRelationFieldToGetter.getMethodParamTypes());
                        } else {
                            for (BaseEntity toEntity : relationToEntitiesInsertedOrUpdated) {
                                if (callReflectionMethodQuick(toEntity, dbEntityColumnToFieldToGetter.getReferenceToColumnClassFieldGetterMethodName()).equals(entity.getId())) {
                                    toAdd.add(toEntity);
                                }
                            }
                            callReflectionMethodQuick(entity, dbEntityColumnToFieldToGetter.getSetterMethodName(), new Object[]{toAdd}, dbEntityColumnToFieldToGetter.getMethodParamTypes());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_INSERT_OR_UPDATE_SUB_ENTITIES__ERROR,e);
        }
        return relationToEntities;
    }

    protected Connection getConnection() throws SQLException {
        if(connection == null || connection.isClosed()) {
            Map<String, String> dbConnectionDetails = getDbConnectionDetails();

            connection = DriverManager.getConnection(
                    dbConnectionDetails.get("DatabaseUrl"),
                    dbConnectionDetails.get("User"),
                    dbConnectionDetails.get("Password")
            );
            connection.setAutoCommit(true);
        }
        return connection;
    }

    protected BaseBeanListHandler<E> getQueryResultHandler() throws RepositoryException {
        try {
            return new BaseBeanListHandler<>(getDbEntityClass());
        } catch (IntrospectionException | IOException | InterruptedException e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_PREPARE_CLASS__ERROR, e);
        }
    }

    protected QueryRunner getRunner() {
        return new QueryRunner();
    }

    private String getPrimaryKeyDbColumnName(Collection<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters) throws RepositoryException {
        return dbEntityColumnToFieldToGetters.stream()
                .filter(DbEntityColumnToFieldToGetter::isPrimaryKey)
                .findFirst().orElseThrow(() -> new RepositoryException(RepositoryError.REPOSITORY_UPDATE_ENTITY__ERROR, "unable to determine primaryKey"))
                .getDbColumnName();
    }

    private String getPrimaryKeyDbColumnName(Class<? extends BaseEntity> dbEntityClass) throws RepositoryException {
        try {
            return getDbEntityColumnToFieldToGetters(dbEntityClass).stream()
                    .filter(DbEntityColumnToFieldToGetter::isPrimaryKey)
                    .findFirst().orElseThrow(() -> new RepositoryException(RepositoryError.REPOSITORY_UPDATE_ENTITY__ERROR, "unable to determine primaryKey"))
                    .getDbColumnName();
        } catch(IntrospectionException e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_RUN_QUERY__ERROR,e.getMessage());
        }
    }

    private boolean includeDbEntityColumnToFieldToGetterInInsertOrUpdateOperations(DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter) {
        return (
           dbEntityColumnToFieldToGetter.hasSetter() &&
           !dbEntityColumnToFieldToGetter.isPrimaryKey() &&
           !dbEntityColumnToFieldToGetter.isForOneToManyRelation() &&
           !dbEntityColumnToFieldToGetter.isForManyToOneRelation() &&
           !dbEntityColumnToFieldToGetter.isForOneToOneRelation()
        );
    }

    public enum QueryType {
        INSERT,
        UPDATE,
        DELETE,
        GET
    }

    public enum OrderBy {
        DESC("DESC"),
        ASC("ASC");

        private final String queryBase;

        OrderBy(String queryBase) {
            this.queryBase = queryBase;
        }
        public String getQueryEquivalent(String relevantFieldName) {
            switch(this) {
                case DESC:
                case ASC:
                    return " ORDER BY " + relevantFieldName + " " + queryBase;
                default:
                    return queryBase;
            }
        }
    }

    private boolean isEmptyOrBlankCollection(Collection<?> collection) {
        boolean result = true;
        if(collection.isEmpty()) {
            return true;
        }
        for (Object obj: collection) {
            if (obj != null) {
                result = false;
                break;
            }
        }
        return result;
    }

}
