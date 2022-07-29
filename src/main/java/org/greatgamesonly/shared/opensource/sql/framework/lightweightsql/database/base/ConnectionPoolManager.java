package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.DbConnectionManager.getDatabaseMaxDbConnectionPool;

class ConnectionPoolManager {

    private static final ArrayList<PooledConnection> connectionPool = new ArrayList<>();

    static final HashMap<String, Boolean> connectionPoolInUseStatuses = new HashMap<>();

    private static final int connectionOpenHours = 1;

    static Timer managerTimer;

    static Timer dbConnectionPoolMonitorTimer;

    static int managerTimerIntervalSeconds = 15;

    static int timesManagerTimerMustRunBeforePoolSizeReAdjustment = 1;

    static int timesManagerTimerRan = 0;

    static ArrayList<Long> totalUsedConnectionsEverySecondBeforeReAdjustment = new ArrayList<>();

    static int currentDbConnectionPoolSize;

    static Timer startManager() {
        if(managerTimer == null) {
            currentDbConnectionPoolSize = getDatabaseMaxDbConnectionPool();
            try {
                setConnectionPool();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            Timer timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            if(timesManagerTimerRan >= timesManagerTimerMustRunBeforePoolSizeReAdjustment) {
                                timesManagerTimerRan = 0;
                                int averageActiveConnectionsInPast = new Double(Math.ceil(totalUsedConnectionsEverySecondBeforeReAdjustment.stream().mapToDouble(a -> a)
                                        .average().orElse(1D))+1D).intValue();
                                currentDbConnectionPoolSize = averageActiveConnectionsInPast+1;
                                if(currentDbConnectionPoolSize > getDatabaseMaxDbConnectionPool()) {
                                    currentDbConnectionPoolSize = getDatabaseMaxDbConnectionPool();
                                }
                                totalUsedConnectionsEverySecondBeforeReAdjustment.clear();
                            }
                            connectionPool.removeIf((connection) -> {
                                boolean mustCloseAndRemove = !connectionPoolInUseStatuses.get(connection.getUniqueReference()) &&
                                        (
                                                connection.getTimeConnectionOpened().before(DbUtils.nowDbTimestamp(connectionOpenHours)) ||
                                                !isDbConnected(connection.getConnection()) ||
                                                connectionPool.size() > currentDbConnectionPoolSize
                                        );
                                if(mustCloseAndRemove) {
                                    try {
                                        org.apache.commons.dbutils.DbUtils.close(connection.getConnection());
                                        connectionPoolInUseStatuses.remove(connection.getUniqueReference());
                                    } catch (SQLException ignored) {
                                        System.out.println("lightweightsql - ConnectionPoolManager -> unable to close a db connection properly");
                                    }
                                }
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException ignored) {}
                                return mustCloseAndRemove;
                            });
                            try {
                                setConnectionPool();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            timesManagerTimerRan++;
                        }
                    },
                    1000L, managerTimerIntervalSeconds * 1000L
            );
            managerTimer = timer;
        }
        if(dbConnectionPoolMonitorTimer == null) {
            Timer timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            totalUsedConnectionsEverySecondBeforeReAdjustment.add(connectionPool.stream()
                                .filter(pooledConnection -> connectionPoolInUseStatuses.get(pooledConnection.getUniqueReference()) != null && connectionPoolInUseStatuses.get(pooledConnection.getUniqueReference()))
                                .count()
                            );
                        }
                    },
                    100L, 1000L
            );
            dbConnectionPoolMonitorTimer = timer;
        }
        return managerTimer;
    }

    static List<PooledConnection> getConnectionPool() {
        return connectionPool;
    }

    static void setConnectionPool() throws SQLException {
        if(connectionPool.isEmpty() || connectionPool.size() < currentDbConnectionPoolSize) {
            int maxConnectionsToOpen = currentDbConnectionPoolSize - connectionPool.size();
            while(maxConnectionsToOpen > 0) {
                Connection connection = DriverManager.getConnection(
                        DbConnectionManager.getDbConnectionDetails().get("DatabaseUrl"),
                        DbConnectionManager.getDbConnectionDetails().get("User"),
                        DbConnectionManager.getDbConnectionDetails().get("Password")
                );
                connection.setAutoCommit(true);
                String uniqueReference = UUID.randomUUID().toString();
                connectionPool.add(new PooledConnection(connection, DbUtils.nowDbTimestamp(),uniqueReference));
                connectionPoolInUseStatuses.put(uniqueReference,false);
                maxConnectionsToOpen--;
            }
        }
    }

    static PooledConnection getConnection() {
        PooledConnection pooledConnection = getConnectionPool().stream()
            .filter(pooledCon -> isDbConnected(pooledCon.getConnection()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("lightweightsql - ConnectionPoolManager -> no pooled db connection could be fetched for use"));
        connectionPoolInUseStatuses.put(pooledConnection.getUniqueReference(),true);
        return pooledConnection;
    }

    private static boolean isDbConnected(Connection con) {
        try {
            return con != null && !con.isClosed();
        } catch (SQLException ignored) {}

        return false;
    }

}
