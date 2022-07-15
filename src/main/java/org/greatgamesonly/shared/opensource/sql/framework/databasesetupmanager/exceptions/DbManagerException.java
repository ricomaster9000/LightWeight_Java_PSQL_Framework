package org.greatgamesonly.shared.opensource.sql.framework.databasesetupmanager.exceptions;


import org.greatgamesonly.shared.opensource.sql.framework.databasesetupmanager.exceptions.errors.DbManagerError;

public class DbManagerException extends Exception {
    private final DbManagerError type;
    private Exception innerException;

    public DbManagerException(DbManagerError type, Exception e) {
        super(type.getReason()+","+e.getMessage());
        this.type = type;
        this.innerException = e;
    }

    public DbManagerException(DbManagerError type, String message) {
        super(type.getReason()+","+message);
        this.type = type;
    }

    public DbManagerException(DbManagerError type, String message, Exception e) {
        super(type.getReason()+","+message,e);
        this.type = type;
        this.innerException = e;
    }

    public DbManagerError getErrorType() {
        return type;
    }
}
