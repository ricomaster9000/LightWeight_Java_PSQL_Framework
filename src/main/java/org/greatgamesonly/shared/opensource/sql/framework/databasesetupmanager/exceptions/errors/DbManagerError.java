package org.greatgamesonly.shared.opensource.sql.framework.databasesetupmanager.exceptions.errors;


public class DbManagerError extends CustomError {

    public static final DbManagerError REPOSITORY_GET__ERROR = new DbManagerError(DbManagerError.class.getName()+"_00100", "Error getting entity data from database",
            500);
    public static final DbManagerError REPOSITORY_RUN_QUERY__ERROR = new DbManagerError(DbManagerError.class.getName()+"_00101", "Error running query on database",
            500);
    public static final DbManagerError REPOSITORY_INSERT__ERROR = new DbManagerError(DbManagerError.class.getName()+"_00102", "Error inserting entity into database",
            500);
    public static final DbManagerError REPOSITORY_PREPARE_INSERT__ERROR = new DbManagerError(DbManagerError.class.getName()+"_00102b", "Error preparing insert entity into database",
            500);
    public static final DbManagerError REPOSITORY_INSERT_CONSTRAINT_VIOLATION_ERROR = new DbManagerError(DbManagerError.class.getName()+"_00102c", "Error inserting entity into database because inserted data violates a unique db constraint",
            500);
    public static final DbManagerError REPOSITORY_PREPARE_CLASS__ERROR = new DbManagerError(DbManagerError.class.getName()+"_00103", "Error getting repository class data ready",
            500);
    public static final DbManagerError REPOSITORY_UPDATE_ENTITY__ERROR = new DbManagerError(DbManagerError.class.getName()+"_00104", "Error when updating entities",
            500);
    public static final DbManagerError REPOSITORY_DELETE_ENTITY__ERROR = new DbManagerError(DbManagerError.class.getName()+"_00105", "Error when deleting entities",
            500);
    public static final DbManagerError REPOSITORY_UPDATE_ENTITY_WITH_ENTITY__ERROR = new DbManagerError(DbManagerError.class.getName()+"_00106", "Error when updating an entity with an entity",
            500);
    public static final DbManagerError REPOSITORY_COUNT_BY_FIELD__ERROR = new DbManagerError(DbManagerError.class.getName()+"_00107", "Error counting number of records in database by column name",
            500);
    public static final DbManagerError REPOSITORY_CALL_REFLECTION_METHOD__ERROR = new DbManagerError(DbManagerError.class.getName()+"_00108", "Error calling method via reflection",
            500);
    public static final DbManagerError UNABLE_TO_FETCH_SEED_FILES = new DbManagerError(DbManagerError.class.getName()+"_00109", "Error occurred while trying to read seed files",
            500);
    public static final DbManagerError UNABLE_TO_FETCH_MIGRATION_FILES = new DbManagerError(DbManagerError.class.getName()+"_00109", "Error occurred while trying to read seed files",
            500);

    DbManagerError(String errorCode, String reason, int httpStatusCode) {
        super(errorCode,reason,httpStatusCode);
    }
}
