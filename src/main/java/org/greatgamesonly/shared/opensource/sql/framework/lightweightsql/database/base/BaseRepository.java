package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.errors.RepositoryError;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.TableName;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.RepositoryException;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbEntityColumnToFieldToGetter;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.ReflectionUtils.callReflectionMethod;

public abstract class BaseRepository<E extends BaseEntity> {

    private static Connection connection;

    protected abstract Class<E> getDbEntityClass();

    public E getById(Long id) throws RepositoryException {
        List<E> entitiesRetrieved = executeGetQuery("SELECT * FROM " + getDbEntityClass().getAnnotation(TableName.class).value() + " WHERE " + getPrimaryKeyDbColumnName(getDbEntityClass()) + " = " + id);
        return (entitiesRetrieved != null && !entitiesRetrieved.isEmpty()) ? entitiesRetrieved.get(0) : null;
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

    protected List<E> executeDeleteQuery(String queryToRun, Object... queryParameters) throws RepositoryException {
        return executeQuery(queryToRun, QueryType.DELETE, queryParameters);
    }

    private List<E> executeQuery(String queryToRun, QueryType queryType, Object... queryParameters) throws RepositoryException {
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

    protected ResultSet executeGetQueryRaw(String queryToRun) throws RepositoryException {
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

    @SafeVarargs
    public final List<E> insertEntities(E... entitiesToInsert) throws RepositoryException {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if(entitiesToInsert == null || entitiesToInsert.length <= 0) {
                throw new RepositoryException(RepositoryError.REPOSITORY_INSERT__ERROR, "null or empty entitiesToInsert value was passed");
            }
            List<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.getDbEntityColumnToFieldToGetters(getDbEntityClass());

            stringBuilder.append(String.format("INSERT INTO %s (", entitiesToInsert[0].getClass().getAnnotation(TableName.class).value()));
            stringBuilder.append(
                dbEntityColumnToFieldToGetters.stream()
                .filter(dbEntityColumnToFieldToGetter -> dbEntityColumnToFieldToGetter.hasSetter() && !dbEntityColumnToFieldToGetter.isPrimaryKey())
                .map(DbEntityColumnToFieldToGetter::getDbColumnName)
                .collect(Collectors.joining(","))
            );
            stringBuilder.append(") VALUES ");
            for (E entityToInsert : entitiesToInsert) {
                stringBuilder.append("(");
                stringBuilder.append(
                        dbEntityColumnToFieldToGetters.stream()
                        .filter(dbEntityColumnToFieldToGetter -> dbEntityColumnToFieldToGetter.hasSetter() && !dbEntityColumnToFieldToGetter.isPrimaryKey())
                        .map(dbEntityColumnToFieldToGetter -> {
                            Object getterValue = callReflectionMethod(entityToInsert, dbEntityColumnToFieldToGetter.getGetterMethodName());
                            return (getterValue != null) ? org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.returnPreparedValueForQuery(getterValue) : null;
                        })
                        .collect(Collectors.joining(","))
                );
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

    @SafeVarargs
    public final List<E> updateEntities(E... entitiesToUpdate) throws RepositoryException {
        List<E> result = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if(entitiesToUpdate == null || entitiesToUpdate.length <= 0) {
                throw new RepositoryException(RepositoryError.REPOSITORY_INSERT__ERROR, "null or empty entitiesToUpdate value was passed");
            }
            List<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.getDbEntityColumnToFieldToGetters(getDbEntityClass());
            String primaryKeyColumnName = getPrimaryKeyDbColumnName(dbEntityColumnToFieldToGetters);

            stringBuilder.append(String.format("UPDATE %s SET ", entitiesToUpdate[0].getClass().getAnnotation(TableName.class).value()));
            for (E entityToUpdate : entitiesToUpdate) {
                stringBuilder.append(
                        dbEntityColumnToFieldToGetters.stream()
                        .filter(dbEntityColumnToFieldToGetter -> dbEntityColumnToFieldToGetter.hasSetter() && !dbEntityColumnToFieldToGetter.isPrimaryKey())
                        .map(dbEntityColumnToFieldToGetter -> {
                            Object getterValue = callReflectionMethod(entityToUpdate, dbEntityColumnToFieldToGetter.getGetterMethodName());
                            return dbEntityColumnToFieldToGetter.getDbColumnName() + " = " + ((getterValue != null) ? org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.returnPreparedValueForQuery(getterValue) : null);
                        })
                        .collect(Collectors.joining(","))
                );
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
            List<DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.getDbEntityColumnToFieldToGetters(getDbEntityClass());
            String primaryKeyColumnName = getPrimaryKeyDbColumnName(dbEntityColumnToFieldToGetters);

            stringBuilder.append(String.format("DELETE * FROM %s WHERE %s IN ( ", entitiesToDelete[0].getClass().getAnnotation(TableName.class).value(), primaryKeyColumnName));
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
                    dbConnectionDetails.get("DatabaseName"),
                    dbConnectionDetails.get("User"),
                    dbConnectionDetails.get("Password")
            );
            connection.setAutoCommit(true);
        }
        return connection;
    }

    abstract Map<String, String> getDbConnectionDetails();

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

    private String getPrimaryKeyDbColumnName(Class<E> dbEntityClass) throws RepositoryException {
        try {
            return org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.getDbEntityColumnToFieldToGetters(dbEntityClass).stream()
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

}
