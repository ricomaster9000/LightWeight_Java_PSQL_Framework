package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.errors;


public class RepositoryError extends CustomError {

    public static final RepositoryError REPOSITORY_GET__ERROR = new RepositoryError(RepositoryError.class.getName()+"_00100", "Error getting entity data from database",
            500);
    public static final RepositoryError REPOSITORY_RUN_QUERY__ERROR = new RepositoryError(RepositoryError.class.getName()+"_00101", "Error running query on database",
            500);
    public static final RepositoryError REPOSITORY_INSERT__ERROR = new RepositoryError(RepositoryError.class.getName()+"_00102", "Error inserting entity into database",
            500);
    public static final RepositoryError REPOSITORY_PREPARE_INSERT__ERROR = new RepositoryError(RepositoryError.class.getName()+"_00102b", "Error preparing insert entity into database",
            500);
    public static final RepositoryError REPOSITORY_INSERT_CONSTRAINT_VIOLATION_ERROR = new RepositoryError(RepositoryError.class.getName()+"_00102c", "Error inserting entity into database because inserted data violates a unique db constraint",
            500);
    public static final RepositoryError REPOSITORY_PREPARE_CLASS__ERROR = new RepositoryError(RepositoryError.class.getName()+"_00103", "Error getting repository class data ready",
            500);
    public static final RepositoryError REPOSITORY_UPDATE_ENTITY__ERROR = new RepositoryError(RepositoryError.class.getName()+"_00104", "Error when updating entities",
            500);
    public static final RepositoryError REPOSITORY_DELETE_ENTITY__ERROR = new RepositoryError(RepositoryError.class.getName()+"_00105", "Error when deleting entities",
            500);
    public static final RepositoryError REPOSITORY_SEARCH_CURRENCY_CONVERSION_ERROR = new RepositoryError(RepositoryError.class.getName()+"_00106", "Error when searching for Currency Conversion in database",
            500);
    public static final RepositoryError REPOSITORY_COUNT_TOTAL_COMPANIES_ERROR = new RepositoryError(RepositoryError.class.getName()+"_00107", "Error when counting all Companies in database",
            500);
    public static final RepositoryError REPOSITORY_UNABLE_TO_MERGE_ENTITY_TO_UPDATE_WITH_ENTITY_THAT_EXISTS__ERROR = new RepositoryError(RepositoryError.class.getName()+"_00108", "Error merging entity to update with entity that already exists",
            500);

    RepositoryError(String errorCode, String reason, int httpStatusCode) {
        super(errorCode,reason,httpStatusCode);
    }
}
