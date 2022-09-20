package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;

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
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.greatgamesonly.reflection.utils.ReflectionUtils.*;
import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.*;
import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.DbConnectionPoolManager.connectionPoolInUseStatuses;
import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.errors.RepositoryError.REPOSITORY_DETERMINE_PRIMARY_KEY_COLUMN__ERROR;

public abstract class BaseRepository<E extends BaseEntity> {
    private Class<E> dbEntityClass;
    private Class<E[]> dbEntityArrayClass;

    private String dbEntityTableName;

    private String primaryKeyColumnName;

    private Repository repoAnnotation;

    private Entity entityAnnotation;

    private BaseBeanListHandler<E> queryHandler;


    private final static String COUNT_BY_COLUMN_QUERY_UNFORMATTED = "SELECT COUNT(*) as total FROM %s WHERE %s = %s;";
    private final static String GET_MAX_VALUE_QUERY_UNFORMATTED = "SELECT MAX(%s) as max FROM %s;";
    private final static String GET_MAX_VALUE_BY_COLUMN_QUERY_UNFORMATTED = "SELECT MAX(%s) as max FROM %s WHERE %s = %s;";
    private final static String COUNT_BY_COLUMNS_TWO_QUERY_UNFORMATTED = "SELECT COUNT(*) as total FROM %s WHERE %s = %s AND %s = %s;";
    private final static String GET_BY_COLUMN_NAME_QUERY_UNFORMATTED = "SELECT * FROM %s WHERE %s = %s";
    private final static String GET_BY_COLUMNS_TWO_NAME_QUERY_UNFORMATTED = "SELECT * FROM %s WHERE %s = %s AND %s = %s";
    private final static String GET_ALL_QUERY_UNFORMATTED = "SELECT * FROM %s";
    private final static String GET_ALL_ORDER_BY_COLUMN_QUERY_UNFORMATTED = "SELECT * FROM %s%s";
    private final static String GET_BY_COLUMN_GREATER_AS_QUERY_UNFORMATTED = "SELECT * FROM %s WHERE %s > %s";
    private final static String GET_BY_COLUMN_LESSER_AS_QUERY_UNFORMATTED = "SELECT * FROM %s WHERE %s < %s";
    private final static String GET_BY_COLUMN_GREATER_AS_ODER_BY_QUERY_UNFORMATTED = "SELECT * FROM %s WHERE %s > %s%s";
    private final static String GET_BY_COLUMN_LESSER_AS_ODER_BY_QUERY_UNFORMATTED = "SELECT * FROM %s WHERE %s < %s%s";
    private final static String DELETE_BY_COLUMN_QUERY_UNFORMATTED = "DELETE FROM %s WHERE %s = %s";

    public BaseRepository() {
        DbConnectionPoolManager.startManager();
    }

    public Class<E> getDbEntityClass() {
        if(dbEntityClass == null) {
            dbEntityClass = (Class<E>) getRepositoryAnnotation().dbEntityClass();
        }
        return dbEntityClass;
    }

    public Repository getRepositoryAnnotation() {
        try {
            if (repoAnnotation == null) {
                if (this.getClass().isAnnotationPresent(Repository.class)) {
                    repoAnnotation = this.getClass().getAnnotation(Repository.class);
                } else if (this.getClass().getName().endsWith("_Subclass")/*Quarkus_Support*/) {
                    repoAnnotation = getClassByName(this.getClass().getName().replaceAll("_Subclass", "")).getAnnotation(Repository.class);
                }
                if(repoAnnotation == null || repoAnnotation.dbEntityClass() == null) {
                    throw new Exception(String.format("Linked Repository annotation to class %s is invalid",this.getClass()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("unable to fetch details from Repository annotation for repository, make sure it was set with correct info, "+e.getMessage());
        }
        return repoAnnotation;
    }

    public Entity getEntityAnnotation() {
            if(entityAnnotation == null) {
                Repository repositoryAnnotation = getRepositoryAnnotation();
                try {
                    entityAnnotation = repositoryAnnotation.dbEntityClass().getAnnotation(Entity.class);
                    if(entityAnnotation == null || entityAnnotation.tableName() == null || entityAnnotation.tableName().isBlank() || entityAnnotation.repositoryClass() == null) {
                        throw new Exception(String.format("Linked Entity annotation to class %s is invalid",this.getClass()));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("unable to fetch details from Entity annotation for repository, make sure it was set with correct info, "+e.getMessage());
                }
            }
        return entityAnnotation;
    }

    public Class<E[]> getDbEntityArrayClass() {
        if(dbEntityArrayClass == null) {
            dbEntityArrayClass = (Class<E[]>) Array.newInstance(getDbEntityClass(),0).getClass();
        }
        return dbEntityArrayClass;
    }

    public String getDbEntityTableName() {
        if(dbEntityTableName == null) {
            dbEntityTableName = getEntityAnnotation().tableName();
        }
        return dbEntityTableName;
    }

    public E getById(Long id) throws RepositoryException {
        List<E> entitiesRetrieved = executeGetQuery("SELECT * FROM " + getDbEntityTableName() + " WHERE " + getPrimaryKeyDbColumnName(getDbEntityClass()) + " = " + id);
        return (entitiesRetrieved != null && !entitiesRetrieved.isEmpty()) ? entitiesRetrieved.get(0) : null;
    }

    public List<E> getAll() throws RepositoryException {
        return executeGetQuery(String.format(GET_ALL_QUERY_UNFORMATTED,getDbEntityTableName()));
    }

    public List<E> getAllOrderByColumn(String orderByColumnName, OrderBy orderBy) throws RepositoryException {
        return executeGetQuery(String.format(GET_ALL_ORDER_BY_COLUMN_QUERY_UNFORMATTED,getDbEntityTableName(),orderBy.getQueryEquivalent(orderByColumnName)));
    }

    public List<E> getAllByMinAndMaxAndColumn(Object minId, Object maxId, String columnName) throws RepositoryException {
        return getAllByMinAndMaxAndColumn(minId, maxId, columnName, null);
    }

    public Map<Long,E> getAllByMinAndMaxAndColumnAsMap(Object minId, Object maxId, String columnName) throws RepositoryException {
        return getAllByMinAndMaxAndColumn(minId, maxId, columnName, null).stream().collect(Collectors.toMap(BaseEntity::getId, entity -> entity));
    }

    public List<E> getAllByMinAndMaxAndColumn(Object minId, Object maxId, String columnName, String additionalWhereQuery) throws RepositoryException {
        return executeGetQuery("SELECT * FROM " + getDbEntityTableName() +
                " WHERE " + columnName + " >= " + returnPreparedValueForQuery(minId) +
                " AND " + columnName + " <= " + returnPreparedValueForQuery(maxId) +
                ((additionalWhereQuery != null && !additionalWhereQuery.isBlank()) ? " AND " + additionalWhereQuery : ""));
    }

    public void deleteById(Long id) throws RepositoryException {
        executeDeleteQuery("DELETE FROM " + getDbEntityTableName() + " WHERE " + getPrimaryKeyDbColumnName(getDbEntityClass()) + " = " + id);
    }

    public void deleteByColumn(String columnName, Object columnValue) throws RepositoryException {
        executeDeleteQuery(String.format(DELETE_BY_COLUMN_QUERY_UNFORMATTED,getDbEntityTableName(),columnName,returnPreparedValueForQuery(columnValue)));
    }

    public List<E> getByColumn(String columnName, Object columnValue) throws RepositoryException {
        return executeGetQuery(String.format(GET_BY_COLUMN_NAME_QUERY_UNFORMATTED,getDbEntityTableName(),columnName,returnPreparedValueForQuery(columnValue)));
    }

    public List<E> getByColumns(String columnName, Object columnValue, String columnName2, Object columnValue2) throws RepositoryException {
        return executeGetQuery(String.format(
                GET_BY_COLUMNS_TWO_NAME_QUERY_UNFORMATTED,
                getDbEntityTableName(),
                columnName,
                returnPreparedValueForQuery(columnValue),
                columnName2,
                returnPreparedValueForQuery(columnValue2)
        ));
    }

    public E getSingleEntityByColumn(String columnName, Object columnValue) throws RepositoryException {
        List<E> entitiesRetrieved = getByColumn(columnName, columnValue);
        return (entitiesRetrieved != null && !entitiesRetrieved.isEmpty()) ? entitiesRetrieved.get(0) : null;
    }

    public E getSingleEntityByColumns(String columnName, Object columnValue, String columnName2, Object columnValue2) throws RepositoryException {
        List<E> entitiesRetrieved = getByColumns(columnName, columnValue, columnName2, columnValue2);
        return (entitiesRetrieved != null && !entitiesRetrieved.isEmpty()) ? entitiesRetrieved.get(0) : null;
    }

    public List<E> getByColumnGreaterAs(String columnName, Object columnValueGreaterAs) throws RepositoryException {
        List<E> entitiesRetrieved = executeGetQuery(String.format(GET_BY_COLUMN_GREATER_AS_QUERY_UNFORMATTED,
            getDbEntityTableName(),
            columnName,
            returnPreparedValueForQuery(columnValueGreaterAs)
        ));
        return entitiesRetrieved;
    }

    public List<E> getByColumnLesserAs(String columnName, Object columnValueLesserAs) throws RepositoryException {
        List<E> entitiesRetrieved = executeGetQuery(String.format(GET_BY_COLUMN_LESSER_AS_QUERY_UNFORMATTED,
                getDbEntityTableName(),
                columnName,
                returnPreparedValueForQuery(columnValueLesserAs)
        ));
        return entitiesRetrieved;
    }

    public List<E> getByColumnGreaterAsOrderByColumn(String columnName, Object columnValueGreaterAs, String columnNameOrderBy, OrderBy orderBy) throws RepositoryException {
        List<E> entitiesRetrieved = executeGetQuery(String.format(GET_BY_COLUMN_GREATER_AS_ODER_BY_QUERY_UNFORMATTED,
                getDbEntityTableName(),
                columnName,
                returnPreparedValueForQuery(columnValueGreaterAs),
                orderBy.getQueryEquivalent(columnNameOrderBy)
        ));
        return entitiesRetrieved;
    }

    public List<E> getByColumnLesserAsOrderByColumn(String columnName, Object columnValueLesserAs, String columnNameOrderBy, OrderBy orderBy) throws RepositoryException {
        List<E> entitiesRetrieved = executeGetQuery(String.format(GET_BY_COLUMN_LESSER_AS_ODER_BY_QUERY_UNFORMATTED,
                getDbEntityTableName(),
                columnName,
                returnPreparedValueForQuery(columnValueLesserAs),
                orderBy.getQueryEquivalent(columnNameOrderBy)
        ));
        return entitiesRetrieved;
    }

    public List<E> getByColumnOrderByPrimaryKey(String columnName, Object columnValue, OrderBy descOrAsc) throws RepositoryException {
        List<E> entitiesRetrieved = executeGetQuery("SELECT * FROM " +
                getDbEntityTableName() + " WHERE " + columnName + " = " +
                returnPreparedValueForQuery(columnValue) +
                descOrAsc.getQueryEquivalent(getPrimaryKeyDbColumnName(getDbEntityClass())));
        return entitiesRetrieved;
    }

    public E getSingleEntityByColumnOrderByPrimaryKey(String columnName, Object columnValue, OrderBy descOrAsc) throws RepositoryException {
        List<E> entitiesRetrieved = getByColumnOrderByPrimaryKey(columnName, columnValue, descOrAsc);
        return (entitiesRetrieved != null && !entitiesRetrieved.isEmpty()) ? entitiesRetrieved.get(0) : null;
    }

    public List<E> getByColumnOrderByColumn(String columnName, Object columnValue, String orderByColumn, OrderBy descOrAsc) throws RepositoryException {
        List<E> entitiesRetrieved = executeGetQuery("SELECT * FROM " +
                getDbEntityTableName() + " WHERE " + columnName + " = " +
                returnPreparedValueForQuery(columnValue) +
                descOrAsc.getQueryEquivalent(orderByColumn));
        return entitiesRetrieved;
    }

    public Long getMaxValueForColumn(String maxColumnName) throws RepositoryException {
        try {
            long countTotal;
            ResultSet resultSet = executeQueryRaw(String.format(GET_MAX_VALUE_QUERY_UNFORMATTED,maxColumnName,getDbEntityTableName()));
            resultSet.next();
            countTotal = resultSet.getLong("max");
            resultSet.close();
            return countTotal;
        } catch (SQLException e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_GET_MAX_FOR_FIELD__ERROR,e);
        }
    }

    public Long getMaxValueForColumnByColumn(String maxColumnName, String byColumnName, String byColumnValue) throws RepositoryException {
        try {
            long countTotal;
            ResultSet resultSet = executeQueryRaw(String.format(GET_MAX_VALUE_BY_COLUMN_QUERY_UNFORMATTED,
                    maxColumnName,
                    getDbEntityTableName(),
                    byColumnName,
                    byColumnValue
            ));
            resultSet.next();
            countTotal = resultSet.getLong("max");
            resultSet.close();
            return countTotal;
        } catch (SQLException e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_GET_MAX_FOR_FIELD_BY_COLUMN__ERROR,e);
        }
    }

    public Long countByColumn(String columnName, Object columnKey) throws RepositoryException {
        try {
            long countTotal;
            ResultSet resultSet = executeQueryRaw(String.format(COUNT_BY_COLUMN_QUERY_UNFORMATTED,
                    getDbEntityTableName(),
                    columnName,
                    returnPreparedValueForQuery(columnKey))
            );
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
            ResultSet resultSet = executeQueryRaw(String.format(
                    COUNT_BY_COLUMNS_TWO_QUERY_UNFORMATTED,
                    getDbEntityTableName(),
                    columnName,returnPreparedValueForQuery(columnKey),
                    columnName2,
                    returnPreparedValueForQuery(columnKey2))
            );
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
        List<E> sortedEntitiesWithIds = entities.stream().filter(entity -> entity.getId() != null).sorted(Comparator.comparingLong(BaseEntity::getId)).collect(Collectors.toList());
        Map<Long, E> existingEntities = new HashMap<>();
        if(!sortedEntitiesWithIds.isEmpty()) {
            existingEntities = getAllByMinAndMaxAndColumnAsMap(sortedEntitiesWithIds.get(0).getId(), sortedEntitiesWithIds.get(entities.size() - 1).getId(), "id");
        }
        Collection<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters;
        try {
            dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());
        } catch (IntrospectionException e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_GENERAL__ERROR, e);
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
        if(getEntityAnnotation().addToWhereForEveryHandledGetQuery() != null && !getEntityAnnotation().addToWhereForEveryHandledGetQuery().isBlank()) {
            queryToRun = addWhereForEveryHandledGetQuery(queryToRun);
        }
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
        PooledConnection pooledConnection = null;
        try {
            pooledConnection = getConnection();
            if(queryType.equals(QueryType.INSERT)) {
                    entityList = getRunner().insert(pooledConnection.getConnection(), queryToRun, getQueryResultHandler(), queryParameters);
            } else if(queryType.equals(QueryType.UPDATE)) {
                    entityList = getRunner().execute(pooledConnection.getConnection(), queryToRun, getQueryResultHandler()).stream().flatMap(List::stream).collect(Collectors.toList());
            } else if(queryType.equals(QueryType.DELETE)) {
                getRunner().execute(pooledConnection.getConnection(), queryToRun, getQueryResultHandler());
            } else if(queryType.equals(QueryType.GET)) {
                entityList = getRunner().query(pooledConnection.getConnection(), queryToRun, getQueryResultHandler(), queryParameters);
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
                        List<? extends BaseEntity> toRelationEntities = relationEntityRepo.getAllByMinAndMaxAndColumn(minId, maxId, dbEntityColumnToFieldToGetter.getReferenceToColumnName(), dbEntityColumnToFieldToGetter.getAdditionalQueryToAdd());
                        for (BaseEntity relationEntity : toRelationEntities) {
                            E entityToSetToManyRelationsOn = entityHashMap.get((Long) callReflectionMethodQuick(relationEntity, dbEntityColumnToFieldToGetter.getReferenceToColumnClassFieldGetterMethodName()));
                            callReflectionMethodQuick(entityToSetToManyRelationsOn, dbEntityColumnToFieldToGetter.getSetterMethodName(), new Object[]{relationEntity}, dbEntityColumnToFieldToGetter.getMethodParamTypes());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            if(e.getSQLState() != null && e.getSQLState().startsWith("23505")) {
                throw new RepositoryException(RepositoryError.REPOSITORY_CONSTRAINT_VIOLATION_ERROR, e.getMessage(), e);
            }
            throw new RepositoryException(RepositoryError.REPOSITORY_GENERAL_SQL__ERROR,  String.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage()), e);
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_GENERAL__ERROR, e.getMessage() + " non sql error", e);
        } finally {
            if(pooledConnection != null) {
                connectionPoolInUseStatuses.put(pooledConnection.getUniqueReference(), false);
            }
        }
        return entityList;
    }

    protected ResultSet executeQueryRaw(String queryToRun) throws RepositoryException {
        ResultSet entityList = null;
        PooledConnection pooledConnection = null;
        try {
            pooledConnection = getConnection();
            CallableStatement callStatement = pooledConnection.getConnection().prepareCall(queryToRun);
            if(queryToRun.startsWith("SELECT")) {
                entityList = callStatement.executeQuery();
            } else {
                callStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            throw new RepositoryException(RepositoryError.REPOSITORY_GENERAL_SQL__ERROR,  String.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage()), e);
        } catch (Exception e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_GENERAL__ERROR, e.getMessage() + " non sql error", e);
        } finally {
            if(pooledConnection != null) {
                connectionPoolInUseStatuses.put(pooledConnection.getUniqueReference(), false);
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
            throw new RepositoryException(RepositoryError.REPOSITORY_PREPARE_INSERT__ERROR, e.getMessage());
        }
        return insertEntities(getDbEntityArrayClass().cast(toInsert));
    }

    @SafeVarargs
    public final List<E> insertEntities(E... entitiesToInsert) throws RepositoryException {
        return insertEntities(Arrays.stream(entitiesToInsert).collect(Collectors.toList()));
    }

    public List<E> insertEntities(List<E> entitiesToInsert) throws RepositoryException {
        StringBuilder stringBuilder = new StringBuilder();
        List<DbEntityColumnToFieldToGetter> relationFieldToGetters;
        try {
            if(entitiesToInsert == null || entitiesToInsert.size() <= 0) {
                return new ArrayList<>();
            }
            Collection<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());
            relationFieldToGetters = getAllRelationFieldToGetters(getDbEntityClass());

            // HANDLE ONE-TO-MANY, ONE-TO-ONE, MANY-TO-ONE BEGIN
            handleEntityRelationshipInsertsOrUpdates(relationFieldToGetters,entitiesToInsert);
            // HANDLE ONE-TO-MANY, ONE-TO-ONE, MANY-TO-ONE END

            stringBuilder.append(String.format("INSERT INTO %s (", getDbEntityTableName()));
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
                            if(getterValue == null && dbEntityColumnToFieldToGetter.isModifyDateAutoSet()) {
                                getterValue = nowDbTimestamp(dbEntityColumnToFieldToGetter.getModifyDateAutoSetTimezone());
                            }
                            toAppendValues.add((getterValue != null) ? returnPreparedValueForQuery(getterValue) : null);
                        }
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new RepositoryException(RepositoryError.REPOSITORY_CALL_REFLECTION_METHOD__ERROR,e);
                    }
                }
                stringBuilder.append(String.join(",",toAppendValues));
                stringBuilder.append(")");
                if (!entityToInsert.equals(entitiesToInsert.get(entitiesToInsert.size() - 1))) {
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
            throw new RepositoryException(RepositoryError.REPOSITORY_PREPARE_UPDATE__ERROR, e.getMessage());
        }
        return updateEntities(getDbEntityArrayClass().cast(toUpdate));
    }

    @SafeVarargs
    public final List<E> updateEntities(E... entitiesToUpdate) throws RepositoryException {
        return updateEntities(Arrays.stream(entitiesToUpdate).collect(Collectors.toList()));
    }

    public final List<E> updateEntities(List<E> entitiesToUpdate) throws RepositoryException {
        List<E> result = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        List<DbEntityColumnToFieldToGetter> relationFieldToGetters;
        try {
            if(entitiesToUpdate == null || entitiesToUpdate.size() <= 0) {
                return new ArrayList<>();
            }
            Collection<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());
            relationFieldToGetters = getAllRelationFieldToGetters(getDbEntityClass());

            // HANDLE ONE-TO-MANY, ONE-TO-ONE, MANY-TO-ONE BEGIN
            handleEntityRelationshipInsertsOrUpdates(relationFieldToGetters,entitiesToUpdate);
            // HANDLE ONE-TO-MANY, ONE-TO-ONE, MANY-TO-ONE END

            stringBuilder.append(String.format("UPDATE %s SET ", getDbEntityTableName()));
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
                if (!entityToUpdate.equals(entitiesToUpdate.get(entitiesToUpdate.size() - 1))) {
                    stringBuilder.append(",");
                } else {
                    stringBuilder.append(String.format(" WHERE %s = %d", getPrimaryKeyDbColumnName(), entityToUpdate.getId()));
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
        deleteEntities(Arrays.stream(entitiesToDelete).collect(Collectors.toList()));
    }

    public final void deleteEntities(List<E> entitiesToDelete) throws RepositoryException {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if(entitiesToDelete == null || entitiesToDelete.size() <= 0) {
                throw new RepositoryException(RepositoryError.REPOSITORY_INSERT__ERROR, "null or empty entitiesToDelete value was passed");
            }
            Collection<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());
            Collection<DbEntityColumnToFieldToGetter> oneToManyRelationFieldToGetters = getOneToManyRelationFieldToGetters(getDbEntityClass());

            if(!oneToManyRelationFieldToGetters.isEmpty()) {
                for(E entityToDelete : entitiesToDelete) {
                    for (DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : oneToManyRelationFieldToGetters) {
                        BaseRepository<?> toEntityRepo = dbEntityColumnToFieldToGetter.getLinkedClassEntity().getAnnotation(Entity.class).repositoryClass().getDeclaredConstructor().newInstance();
                        toEntityRepo.deleteByColumn(dbEntityColumnToFieldToGetter.getReferenceToColumnName(), entityToDelete.getId());
                    }
                }
            }

            stringBuilder.append(String.format("DELETE FROM %s WHERE %s IN ( ", getDbEntityTableName(), getPrimaryKeyDbColumnName()));
            stringBuilder.append(
                entitiesToDelete.stream()
                .map(entity -> entity.getId().toString())
                .collect(Collectors.joining(","))
            );
            stringBuilder.append(" );");
            executeDeleteQuery(stringBuilder.toString());
        } catch (Exception e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_DELETE_ENTITY__ERROR, e);
        }
    }

    private List<?> handleEntityRelationshipInsertsOrUpdates(List<DbEntityColumnToFieldToGetter> relationFieldToGetters, List<E> entitiesParam) throws RepositoryException {
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
                        if (dbEntityColumnToFieldToGetter.isForOneToOneRelation() || dbEntityColumnToFieldToGetter.isForManyToOneRelation()) {
                            relationToEntitiesInsertedOrUpdated.add(callReflectionMethodGeneric(entity, dbEntityColumnToFieldToGetter.getGetterMethodName()));
                        } else {
                            relationToEntitiesInsertedOrUpdated.addAll(callReflectionMethodGeneric(entity, dbEntityColumnToFieldToGetter.getGetterMethodName()));
                        }
                    }
                    if(isEmptyOrBlankCollection(relationToEntitiesInsertedOrUpdated)) {
                        continue;
                    }
                    BaseRepository<?> toEntityRepo = dbEntityColumnToFieldToGetter.getLinkedClassEntity().getAnnotation(Entity.class).repositoryClass().getDeclaredConstructor().newInstance();
                    if(dbEntityColumnToFieldToGetter.isInsertOrUpdateRelationInDbInteractions()) {
                        insertEntities = toEntityRepo.insertEntitiesListGeneric(relationToEntitiesInsertedOrUpdated.stream().filter(toManyEntity -> toManyEntity != null && toManyEntity.getId() == null).collect(Collectors.toList()));
                        updateEntities = toEntityRepo.updateEntitiesListGeneric(relationToEntitiesInsertedOrUpdated.stream().filter(toManyEntity -> toManyEntity != null && toManyEntity.getId() != null).collect(Collectors.toList()));
                        relationToEntitiesInsertedOrUpdated = Stream.concat(insertEntities.stream(), updateEntities.stream()).collect(Collectors.toList());
                    }
                    relationToEntities = Stream.concat(relationToEntities.stream(), relationToEntitiesInsertedOrUpdated.stream()).collect(Collectors.toList());
                    for (E entity : entitiesParam) {
                        List<BaseEntity> toAdd = new ArrayList<>();
                        if(dbEntityColumnToFieldToGetter.isForManyToOneRelation()) {
                            DbEntityColumnToFieldToGetter manyToOneRefIdRelationFieldToGetter = getManyToOneRefIdRelationFieldToGetter(entity.getClass(), dbEntityColumnToFieldToGetter);
                            Long idToUse = relationToEntitiesInsertedOrUpdated.get(0).getId();
                            if(!dbEntityColumnToFieldToGetter.isInsertOrUpdateRelationInDbInteractions() && idToUse == null) {
                                throw new RepositoryException(RepositoryError.REPOSITORY_INSERT_OR_UPDATE_SUB_ENTITIES__ERROR, String.format("manyToOne entity %s linked to %s has no id",dbEntityColumnToFieldToGetter.getLinkedClassEntity().getSimpleName(), entity.getClass().getSimpleName()));
                            }
                            callReflectionMethodQuick(entity, manyToOneRefIdRelationFieldToGetter.getSetterMethodName(), idToUse, manyToOneRefIdRelationFieldToGetter.getMethodParamTypes()[0]);
                        } else if(dbEntityColumnToFieldToGetter.isForOneToOneRelation()) {
                            DbEntityColumnToFieldToGetter oneToOneRefIdRelationFieldToGetter = getOneToOneRefFromRelationColumnToFieldToGetter(entity.getClass(), dbEntityColumnToFieldToGetter);
                            Long idToUse = relationToEntitiesInsertedOrUpdated.get(0).getId();
                            callReflectionMethodQuick(entity, oneToOneRefIdRelationFieldToGetter.getSetterMethodName(), idToUse, oneToOneRefIdRelationFieldToGetter.getMethodParamTypes()[0]);
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

    protected PooledConnection getConnection() throws SQLException {
        return DbConnectionPoolManager.getConnection();
    }

    protected BaseBeanListHandler<E> getQueryResultHandler() throws RepositoryException {
        try {
            if(queryHandler == null) {
                queryHandler = new BaseBeanListHandler<>(getDbEntityClass());
            }
        } catch (IntrospectionException | IOException | InterruptedException e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_PREPARE_CLASS__ERROR, e);
        }
        return queryHandler;
    }

    protected QueryRunner getRunner() {
        return new QueryRunner();
    }

    private String getPrimaryKeyDbColumnName() throws RepositoryException {
        if(primaryKeyColumnName == null) {
            try {
                primaryKeyColumnName = getDbEntityColumnToFieldToGetters(getDbEntityClass()).stream()
                    .filter(DbEntityColumnToFieldToGetter::isPrimaryKey)
                    .findFirst().orElseThrow(() -> new RepositoryException(REPOSITORY_DETERMINE_PRIMARY_KEY_COLUMN__ERROR, "unable to determine primaryKey"))
                    .getDbColumnName();
            } catch (IntrospectionException e) {
                throw new RepositoryException(REPOSITORY_DETERMINE_PRIMARY_KEY_COLUMN__ERROR, e);
            }
        }
        return primaryKeyColumnName;
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
                    return String.format(" ORDER BY %s %s", relevantFieldName, queryBase);
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

    private String addWhereForEveryHandledGetQuery(String queryToAddTo) {
        String formattedQueryWithAdditionalWhere = "";
        String potentialWherePartInQuery = String.format("FROM %s WHERE",getDbEntityTableName());
        if(queryToAddTo.contains(potentialWherePartInQuery)) {
            formattedQueryWithAdditionalWhere = queryToAddTo.replaceFirst(potentialWherePartInQuery, potentialWherePartInQuery + " " + getEntityAnnotation().addToWhereForEveryHandledGetQuery() + " AND");
        } else {
            String potentialNonWherePartInQuery = String.format("FROM %s",getDbEntityTableName());
            formattedQueryWithAdditionalWhere = queryToAddTo.replaceFirst(potentialNonWherePartInQuery, potentialNonWherePartInQuery + " WHERE " + getEntityAnnotation().addToWhereForEveryHandledGetQuery());
        }
        return formattedQueryWithAdditionalWhere;
    }

    private String addWordAfterWords(String whereToAddWordTo, String wordToAdd, String afterWhichWordsOrWordsToAdd) {
        return whereToAddWordTo.replaceFirst(afterWhichWordsOrWordsToAdd, afterWhichWordsOrWordsToAdd + " " + wordToAdd);
    }

}
