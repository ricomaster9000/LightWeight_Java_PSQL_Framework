package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database;

import java.sql.Connection;
import java.sql.Timestamp;

public class PooledConnection {
    private Connection connection;
    private Timestamp timeConnectionOpened;
    private String uniqueReference;

    PooledConnection(Connection connection, Timestamp timeConnectionOpened,String uniqueReference) {
        this.connection = connection;
        this.timeConnectionOpened = timeConnectionOpened;
        this.uniqueReference = uniqueReference;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Timestamp getTimeConnectionOpened() {
        return timeConnectionOpened;
    }

    public void setTimeConnectionOpened(Timestamp timeConnectionOpened) {
        this.timeConnectionOpened = timeConnectionOpened;
    }

    public String getUniqueReference() {
        return uniqueReference;
    }

    public void setUniqueReference(String uniqueReference) {
        this.uniqueReference = uniqueReference;
    }
}
