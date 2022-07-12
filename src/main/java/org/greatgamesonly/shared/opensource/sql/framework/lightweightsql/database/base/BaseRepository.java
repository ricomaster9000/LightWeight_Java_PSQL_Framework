package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.Repository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.errors.RepositoryError;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.Entity;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.RepositoryException;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbEntityColumnToFieldToGetter;

import javax.print.DocFlavor;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.greatgamesonly.reflection.utils.ReflectionUtils.callReflectionMethod;
import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.*;

public abstract class BaseRepository<E extends BaseEntity> {

    private static Connection connection;
    private final Class<E> dbEntityClass;

    public BaseRepository() {
        if(!this.getClass().isAnnotationPresent(Repository.class)) {
            throw new RuntimeException("Repository annotation must be set for repository class");
        }
        dbEntityClass = (Class<E>) this.getClass().getAnnotation(Repository.class).dbEntityClass();
    }

    private Class<E> getDbEntityClass() {
        return dbEntityClass;
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
            ResultSet resultSet = executeQueryRaw("SELECT COUNT(*) FROM " +
                    getDbEntityClass().getAnnotation(Entity.class).tableName() + " WHERE " + columnName + " = " +
                    returnPreparedValueForQuery(columnKey));
            countTotal = resultSet.getLong(0);
            resultSet.close();
            return countTotal;
        } catch (SQLException e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_COUNT_BY_FIELD__ERROR,e);
        }
    }

    public Long countByColumns(String columnName, Object columnKey, String columnName2, Object columnKey2) throws RepositoryException {
        try {
            long countTotal;
            ResultSet resultSet = executeQueryRaw("SELECT COUNT(*) FROM " +
                    getDbEntityClass().getAnnotation(Entity.class).tableName() + " WHERE " + columnName + " = " +
                    returnPreparedValueForQuery(columnKey) +
                    " AND " + columnName2 + " = " + returnPreparedValueForQuery(columnKey2));
            countTotal = resultSet.getLong(0);
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
            List<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters;
            try {
                dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());
            } catch (IntrospectionException e) {
                throw new RepositoryException(RepositoryError.REPOSITORY_UPDATE_ENTITY_WITH_ENTITY__ERROR,e);
            }
            E finalExistingEntity = existingEntity;

            for(DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : dbEntityColumnToFieldToGetters) {
                if(dbEntityColumnToFieldToGetter.canBeUpdatedInDb() && !dbEntityColumnToFieldToGetter.isPrimaryKey()) {
                    try {
                        callReflectionMethod(
                                finalExistingEntity,
                                dbEntityColumnToFieldToGetter.getSetterMethodName(),
                                callReflectionMethod(entity, dbEntityColumnToFieldToGetter.getGetterMethodName())
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

    protected List<E> executeGetQuery(String queryToRun, Object... queryParameters) throws RepositoryException {
        return executeQuery(queryToRun, QueryType.GET, queryParameters);
    }

    protected List<E> executeInsertQuery(String queryToRun, Object... queryParameters) throws RepositoryException {
        return executeQuery(queryToRun, QueryType.INSERT, queryParameters);
    }

    protected List<E> executeUpdateQuery(String queryToRun, Object... queryParameters) throws RepositoryException {
        return executeQuery(queryToRun, QueryType.INSERT, queryParameters);
    }

    protected void executeDeleteQuery(String queryToRun, Object... queryParameters) throws RepositoryException {
        executeQuery(queryToRun, QueryType.DELETE, queryParameters);
    }

    private List<E> executeQuery(String queryToRun, QueryType queryType, Object... queryParameters) throws RepositoryException {
        List<E> entityList = new ArrayList<>();
        try {
            List<DbEntityColumnToFieldToGetter> relationFieldToGetters = Stream.concat(getOneToManyRelationFieldToGetters(getDbEntityClass()).stream(), getOneToOneRelationFieldToGetters(getDbEntityClass()).stream()).collect(Collectors.toList());
            relationFieldToGetters.addAll(getManyToOneRelationFieldToGetters(getDbEntityClass()));

            if(queryType.equals(QueryType.INSERT) || queryType.equals(QueryType.UPDATE)) {
                if(queryType.equals(QueryType.INSERT)) {
                    entityList = getRunner().insert(getConnection(), queryToRun, getQueryResultHandler(), queryParameters);
                } else {
                    entityList = getRunner().execute(getConnection(), queryToRun, getQueryResultHandler()).stream().flatMap(List::stream).collect(Collectors.toList());
                }
                // let us disable this for now, not too sure yet
                if(false) {//if(!oneToManyRelationFieldToGetters.isEmpty()) {
                    for(DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : relationFieldToGetters) {
                        BaseRepository<? extends BaseEntity> toManyRepo = dbEntityColumnToFieldToGetter.getLinkedClassEntity().getAnnotation(Entity.class).repositoryClass().getDeclaredConstructor().newInstance();
                        List<? extends BaseEntity> toManyEntities = new ArrayList<>();
                        List<? extends BaseEntity> insertEntities;
                        List<? extends BaseEntity> updateEntities;
                        for(E entity : entityList) {
                            toManyEntities.addAll(callReflectionMethodGeneric(entity, dbEntityColumnToFieldToGetter.getGetterMethodName()));
                        }
                        insertEntities = toManyRepo.insertEntitiesList(toManyEntities.stream().filter(toManyEntity -> toManyEntity.getId() == null).collect(Collectors.toList()));
                        updateEntities = toManyRepo.updateEntitiesList(toManyEntities.stream().filter(toManyEntity -> toManyEntity.getId() != null).collect(Collectors.toList()));
                        toManyEntities = Stream.concat(insertEntities.stream(), updateEntities.stream()).collect(Collectors.toList());
                        for(E entity : entityList) {
                            List<BaseEntity> toAdd = new ArrayList<>();
                            for(BaseEntity toManyEntity : toManyEntities) {
                                if(callReflectionMethod(toManyEntity, dbEntityColumnToFieldToGetter.getReferenceToColumnClassFieldGetterMethodName()).equals(entity.getId())) {
                                    toAdd.add(toManyEntity);
                                }
                            }
                            callReflectionMethod(entity, dbEntityColumnToFieldToGetter.getSetterMethodName(), toAdd);
                        }
                    }
                }
            } else if(queryType.equals(QueryType.DELETE)) {
                getRunner().execute(getConnection(), queryToRun, getQueryResultHandler());
            } else if(queryType.equals(QueryType.GET)) {
                entityList = getRunner().query(getConnection(), queryToRun, getQueryResultHandler(), queryParameters);
            }
            if(queryType.equals(QueryType.GET)) {
                if (!relationFieldToGetters.isEmpty()) {
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
                        if(dbEntityColumnToFieldToGetter.isForManyToOneRelation()) {
                            continue; // Handled in the BaseBeanListHandler
                        }
                        BaseRepository<? extends BaseEntity> relationEntityRepo = dbEntityColumnToFieldToGetter.getLinkedClassEntity().getAnnotation(Entity.class).repositoryClass().getDeclaredConstructor().newInstance();
                        List<? extends BaseEntity> oneToOneRelationEntities = relationEntityRepo.getAllByMinAndMaxAndColumnName(minId, maxId, dbEntityColumnToFieldToGetter.getReferenceToColumnName(), dbEntityColumnToFieldToGetter.getAdditionalQueryToAdd());
                        for (BaseEntity relationEntity : oneToOneRelationEntities) {
                            E entityToSetToManyRelationsOn = entityHashMap.get((Long) callReflectionMethod(relationEntity, dbEntityColumnToFieldToGetter.getReferenceToColumnClassFieldGetterMethodName()));
                            callReflectionMethod(entityToSetToManyRelationsOn, dbEntityColumnToFieldToGetter.getSetterMethodName(), relationEntity);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            if(e.getSQLState().startsWith("23505")) {
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
    public static <T> T callReflectionMethodGeneric(Object object, String methodName, Object... methodParams) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object methodResult = null;
        if (methodParams != null && methodParams.length != 0) {
            methodResult = object.getClass().getMethod(methodName).invoke(object, methodParams);
        } else {
            methodResult = object.getClass().getMethod(methodName).invoke(object);
        }
        return (T) methodResult;
    }

    protected ResultSet executeQueryRaw(String queryToRun) throws RepositoryException {
        ResultSet entityList;
        try {
            CallableStatement callStatement = getConnection().prepareCall(queryToRun);
            entityList = callStatement.executeQuery();
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

    protected final List<E> insertEntitiesList(List<? extends BaseEntity> entitiesToInsert) throws RepositoryException {
        List<E> es = insertEntities((E[]) entitiesToInsert.toArray());
        return es;
    }

    @SafeVarargs
    public final List<E> insertEntities(E... entitiesToInsert) throws RepositoryException {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if(entitiesToInsert == null || entitiesToInsert.length <= 0) {
                throw new RepositoryException(RepositoryError.REPOSITORY_INSERT__ERROR, "null or empty entitiesToInsert value was passed");
            }
            List<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());

            stringBuilder.append(String.format("INSERT INTO %s (", entitiesToInsert[0].getClass().getAnnotation(Entity.class).tableName()));
            stringBuilder.append(
                dbEntityColumnToFieldToGetters.stream()
                .filter(dbEntityColumnToFieldToGetter ->
                        dbEntityColumnToFieldToGetter.hasSetter() &&
                        !dbEntityColumnToFieldToGetter.isPrimaryKey() &&
                        !dbEntityColumnToFieldToGetter.isForOneToManyRelation() &&
                        !dbEntityColumnToFieldToGetter.isForManyToOneRelation() &&
                        !dbEntityColumnToFieldToGetter.isForOneToOneRelation()
                )
                .map(DbEntityColumnToFieldToGetter::getDbColumnName)
                .collect(Collectors.joining(","))
            );
            stringBuilder.append(") VALUES ");
            for (E entityToInsert : entitiesToInsert) {
                stringBuilder.append("(");

                List<String> toAppendValues = new ArrayList<>();
                for(DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : dbEntityColumnToFieldToGetters) {
                    try {
                        if(dbEntityColumnToFieldToGetter.hasSetter() && !dbEntityColumnToFieldToGetter.isPrimaryKey()) {
                            Object getterValue = callReflectionMethod(entityToInsert, dbEntityColumnToFieldToGetter.getGetterMethodName());
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
            }
        } catch (Exception e) {
            throw new RepositoryException(RepositoryError.REPOSITORY_PREPARE_INSERT__ERROR, e);
        }
        return executeInsertQuery(stringBuilder.toString());
    }

    protected final List<E> updateEntitiesList(List<? extends BaseEntity> entitiesToUpdate) throws RepositoryException {
        return updateEntities((E[]) entitiesToUpdate.toArray());
    }

    @SafeVarargs
    public final List<E> updateEntities(E... entitiesToUpdate) throws RepositoryException {
        List<E> result = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if(entitiesToUpdate == null || entitiesToUpdate.length <= 0) {
                throw new RepositoryException(RepositoryError.REPOSITORY_INSERT__ERROR, "null or empty entitiesToUpdate value was passed");
            }
            List<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());
            String primaryKeyColumnName = getPrimaryKeyDbColumnName(dbEntityColumnToFieldToGetters);

            stringBuilder.append(String.format("UPDATE %s SET ", entitiesToUpdate[0].getClass().getAnnotation(Entity.class).tableName()));
            for (BaseEntity entityToUpdate : entitiesToUpdate) {

                List<String> toAppendValues = new ArrayList<>();
                for(DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : dbEntityColumnToFieldToGetters) {
                    try {
                        if(dbEntityColumnToFieldToGetter.hasSetter() &&
                            !dbEntityColumnToFieldToGetter.isPrimaryKey() &&
                            !dbEntityColumnToFieldToGetter.isForOneToManyRelation() &&
                            !dbEntityColumnToFieldToGetter.isForManyToOneRelation() &&
                            !dbEntityColumnToFieldToGetter.isForOneToOneRelation()
                        ) {
                            Object getterValue = callReflectionMethod(entityToUpdate, dbEntityColumnToFieldToGetter.getGetterMethodName());
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
                result = executeUpdateQuery(stringBuilder.toString());
            }
        } catch (Exception e) {
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
            List<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = getDbEntityColumnToFieldToGetters(getDbEntityClass());
            String primaryKeyColumnName = getPrimaryKeyDbColumnName(dbEntityColumnToFieldToGetters);

            stringBuilder.append(String.format("DELETE FROM %s WHERE %s IN ( ", entitiesToDelete[0].getClass().getAnnotation(Entity.class).tableName(), primaryKeyColumnName));
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

    private String getPrimaryKeyDbColumnName(List<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters) throws RepositoryException {
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

}
