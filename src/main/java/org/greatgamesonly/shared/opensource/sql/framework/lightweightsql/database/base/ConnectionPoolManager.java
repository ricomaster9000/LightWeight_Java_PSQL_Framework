package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

class ConnectionPoolManager {

    private static final ArrayList<PooledConnection> connectionPool = new ArrayList<>();

    private static Map<String, String> connectionDetailsUsed;

    private static final int connectionOpenHours = 1;

    private static boolean managerStarted;

    static Timer managerTimer;

    static Timer startManager(Map<String, String> connectionDetails) {
        if(managerTimer == null) {
            try {
                setConnectionPool(connectionDetails);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            Timer timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            connectionPool.removeIf((connectionPool) -> {
                                boolean mustCloseAndRemove = connectionPool.getTimeConnectionOpened().after(DbUtils.nowDbTimestamp(connectionOpenHours)) || !isDbConnected(connectionPool.getConnection());
                                if(mustCloseAndRemove) {
                                    try {
                                        org.apache.commons.dbutils.DbUtils.close(connectionPool.getConnection());
                                    } catch (SQLException ignored) {}
                                }
                                return mustCloseAndRemove;
                            });
                            try {
                                setConnectionPool(connectionDetails);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    },
                    10 * 60 * 1000
            );
            managerTimer = timer;
        }
        return managerTimer;
    }

    static List<PooledConnection> getConnectionPool() {
        return connectionPool;
    }

    static void setConnectionPool(Map<String, String> connectionDetails) throws SQLException {
        connectionDetailsUsed = connectionDetails;
        if(connectionPool.isEmpty() || connectionPool.size() < 10) {
            int maxConnectionsToOpen = Integer.parseInt(connectionDetails.get("DB_CONNECTION_POOL_SIZE")) - connectionPool.size();
            while(maxConnectionsToOpen > 0) {
                Connection connection = DriverManager.getConnection(
                        connectionDetailsUsed.get("DatabaseUrl"),
                        connectionDetailsUsed.get("User"),
                        connectionDetailsUsed.get("Password")
                );
                connection.setAutoCommit(true);
                connectionPool.add(new PooledConnection(connection, DbUtils.nowDbTimestampPlusMinutes(maxConnectionsToOpen*5)));
                maxConnectionsToOpen--;
            }
        }
    }

    static Connection getConnection() {
        return getConnectionPool().stream()
            .filter(pooledConnection -> isDbConnected(pooledConnection.getConnection()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("no pooled db connection could be fetched for use"))
            .getConnection();
    }

    private static boolean isDbConnected(Connection con) {
        try {
            return con != null && !con.isClosed();
        } catch (SQLException ignored) {}

        return false;
    }

}
