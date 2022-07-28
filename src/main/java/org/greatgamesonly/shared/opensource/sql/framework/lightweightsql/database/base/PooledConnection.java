package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;

import java.sql.Connection;
import java.sql.Timestamp;

public class PooledConnection {
    private Connection connection;
    private Timestamp timeConnectionOpened;

    PooledConnection(Connection connection, Timestamp timeConnectionOpened) {
        this.connection = connection;
        this.timeConnectionOpened = timeConnectionOpened;
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
}
