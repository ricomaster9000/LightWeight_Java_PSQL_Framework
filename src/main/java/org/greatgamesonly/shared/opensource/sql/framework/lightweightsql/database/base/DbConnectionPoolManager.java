package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.DbConnectionDetailsManager.getDatabaseMaxDbConnectionPool;

class DbConnectionPoolManager {

    private static final ArrayList<PooledConnection> connectionPool = new ArrayList<>();

    static final HashMap<String, Boolean> connectionPoolInUseStatuses = new HashMap<>();

    private static final int connectionOpenHours = 1;

    static Timer managerTimer;

    static Timer dbConnectionPoolMonitorTimer;

    static int managerTimerIntervalSeconds = 10;

    static boolean isManagerTimerRunning = false;

    static int timesManagerTimerMustRunBeforePoolSizeReAdjustment = 1;

    static int timesManagerTimerRan = 0;

    static ArrayList<Long> totalUsedConnectionsEverySecondBeforeReAdjustment = new ArrayList<>();

    static int currentMaxDbConnectionPoolSize;

    static Timer startManager() {
        if(managerTimer == null) {
            currentMaxDbConnectionPoolSize = getDatabaseMaxDbConnectionPool();
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
                            if(isManagerTimerRunning) {
                                return;
                            }
                            isManagerTimerRunning = true;
                            if(timesManagerTimerRan >= timesManagerTimerMustRunBeforePoolSizeReAdjustment) {
                                timesManagerTimerRan = 0;
                                ArrayList<Long> totalUsedConnections = new ArrayList<>(totalUsedConnectionsEverySecondBeforeReAdjustment);
                                int averageActiveConnectionsInPast = new Double(Math.ceil(totalUsedConnections.stream().mapToDouble(a -> a)
                                        .average().orElse(0D))+1D).intValue();
                                currentMaxDbConnectionPoolSize = averageActiveConnectionsInPast;
                                if(currentMaxDbConnectionPoolSize > getDatabaseMaxDbConnectionPool()) {
                                    currentMaxDbConnectionPoolSize = getDatabaseMaxDbConnectionPool();
                                }
                                totalUsedConnectionsEverySecondBeforeReAdjustment.clear();
                            }
                            connectionPool.removeIf((connection) -> {
                                boolean mustCloseAndRemove = !connectionPoolInUseStatuses.get(connection.getUniqueReference()) &&
                                        (
                                                connection.getTimeConnectionOpened().before(DbUtils.nowDbTimestamp(connectionOpenHours)) ||
                                                !isDbConnected(connection.getConnection()) ||
                                                connectionPool.size() > currentMaxDbConnectionPoolSize
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
                                    Thread.sleep(getDatabaseMaxDbConnectionPool() <= 125 ? 100 : 50);
                                } catch (InterruptedException ignored) {}
                                return mustCloseAndRemove;
                            });
                            try {
                                setConnectionPool();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            timesManagerTimerRan++;
                            isManagerTimerRunning = false;
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
        if(connectionPool.isEmpty() || connectionPool.size() < currentMaxDbConnectionPoolSize) {
            int maxConnectionsToOpen = currentMaxDbConnectionPoolSize - connectionPool.size();
            while(maxConnectionsToOpen > 0) {
                Connection connection = DriverManager.getConnection(
                        DbConnectionDetailsManager.getDbConnectionDetails().get("DatabaseUrl"),
                        DbConnectionDetailsManager.getDbConnectionDetails().get("User"),
                        DbConnectionDetailsManager.getDbConnectionDetails().get("Password")
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
            .orElse(null);
        // poll till you get a connection
        if(pooledConnection == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            pooledConnection = getConnection();
        }
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
