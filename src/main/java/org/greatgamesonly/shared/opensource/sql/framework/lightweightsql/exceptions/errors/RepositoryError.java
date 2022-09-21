package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.errors;


public class RepositoryError extends CustomError {

    public static final RepositoryError REPOSITORY_GET__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00100", "Error getting entity data from database",
            500);
    public static final RepositoryError REPOSITORY_GENERAL__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00098", "General Error",
            500);
    public static final RepositoryError REPOSITORY_GENERAL_SQL__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00099", "General SQL related error",
            500);
    public static final RepositoryError REPOSITORY_RUN_QUERY__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00101", "Error running query on database",
            500);
    public static final RepositoryError REPOSITORY_INSERT__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00102", "Error inserting entity into database",
            500);
    public static final RepositoryError REPOSITORY_PREPARE_INSERT__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00102b", "Error preparing insert-entity-query-and-related-info",
            500);
    public static final RepositoryError REPOSITORY_CONSTRAINT_VIOLATION_ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00102c", "Error inserting entity into database because inserted data violates a unique db constraint",
            500);
    public static final RepositoryError REPOSITORY_PREPARE_CLASS__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00103", "Error getting repository class data ready",
            500);
    public static final RepositoryError REPOSITORY_UPDATE_ENTITY__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00104", "Error when updating entities",
            500);
    public static final RepositoryError REPOSITORY_DELETE_ENTITY__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00105", "Error when deleting entities",
            500);
    public static final RepositoryError REPOSITORY_UPDATE_ENTITY_WITH_ENTITY__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00106", "Error when updating an entity with an entity",
            500);
    public static final RepositoryError REPOSITORY_COUNT_BY_FIELD__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00107", "Error counting number of records in database by column name",
            500);
    public static final RepositoryError REPOSITORY_CALL_REFLECTION_METHOD__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00108", "Error calling method via reflection",
            500);
    public static final RepositoryError REPOSITORY_INSERT_OR_UPDATE_SUB_ENTITIES__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00109", "Error inserting or updating all sub relational entities linked to main entities to insert or update",
            500);
    public static final RepositoryError REPOSITORY_PREPARE_UPDATE__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00110", "Error preparing update-entity-query-and-related-info",
            500);
    public static final RepositoryError REPOSITORY_DETERMINE_PRIMARY_KEY_COLUMN__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00111", "Error when getting column name for field specified as @PrimaryKey",
            500);
    public static final RepositoryError REPOSITORY_GET_MAX_FOR_FIELD__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00112", "Error getting max value for column",
            500);
    public static final RepositoryError REPOSITORY_GET_MAX_FOR_FIELD_BY_COLUMN__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00113", "Error getting max value for column by column",
            500);
    public static final RepositoryError REPOSITORY_INVALID_PARAM__ERROR = new RepositoryError(RepositoryError.class.getSimpleName()+"_00114", "Invalid SQL query parameter passed to method",
            500);

    RepositoryError(String errorCode, String reason, int httpStatusCode) {
        super(errorCode,reason,httpStatusCode);
    }
}
