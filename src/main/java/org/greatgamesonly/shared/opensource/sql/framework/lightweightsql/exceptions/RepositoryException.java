package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions;


import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.errors.RepositoryError;

public class RepositoryException extends Exception {
    private final RepositoryError type;

    public RepositoryException(RepositoryError type) {
        super(type.getReason());
        this.type = type;
    }

    public RepositoryException(RepositoryError type, Exception e) {
        super(type.getReason()+","+e.getMessage(),e);
        this.type = type;
    }

    public RepositoryException(RepositoryError type, String message) {
        super(type.getReason()+","+message);
        this.type = type;
    }

    public RepositoryException(RepositoryError type, Object message) {
        super(type.getReason()+","+message);
        this.type = type;
    }

    public RepositoryException(RepositoryError type, String message, Exception e) {
        super(type.getReason()+","+message,e);
        this.type = type;
    }

    public RepositoryError getErrorType() {
        return type;
    }
}
