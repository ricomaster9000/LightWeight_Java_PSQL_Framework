package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbConnectionDetailsManager.getDatabaseMaxDbConnectionPool;


final class DbConnectionPoolManager {
    private DbConnectionPoolManager() {}
    private static final ArrayList<PooledConnection> connectionPool = new ArrayList<>();
    protected static final ConcurrentHashMap<String, Boolean> connectionPoolInUseStatuses = new ConcurrentHashMap<>();
    private static final int connectionOpenHours = 1;
    private static Timer managerTimer;
    private static Timer dbConnectionPoolMonitorTimer;
    private static final int managerTimerIntervalSeconds = 10;
    private static boolean isManagerTimerRunning = false;
    private static final int timesManagerTimerMustRunBeforePoolSizeReAdjustment = 1;
    private static int timesManagerTimerRan = 0;
    private static final ArrayList<Long> totalUsedConnectionsEverySecondBeforeReAdjustment = new ArrayList<>();
    private static int currentMaxDbConnectionPoolSize;

    protected static void startManager() {
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
                                currentMaxDbConnectionPoolSize = Double.valueOf(Math.ceil(totalUsedConnectionsEverySecondBeforeReAdjustment.stream().mapToDouble(a -> a)
                                        .average().orElse(0D))+1D).intValue();
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
                            if(isManagerTimerRunning) {
                                return;
                            }
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
    }

    private static List<PooledConnection> getConnectionPool() {
        return connectionPool;
    }

    protected static void setConnectionPool() throws SQLException {
        if(connectionPool.isEmpty() || connectionPool.size() < currentMaxDbConnectionPoolSize) {
            int maxConnectionsToOpen = currentMaxDbConnectionPoolSize - connectionPool.size();
            while(maxConnectionsToOpen > 0) {
                Connection connection = DriverManager.getConnection(
                        DbConnectionDetailsManager.getDbConnectionDetails().get("DATABASE_URL"),
                        DbConnectionDetailsManager.getDbConnectionDetails().get("DATABASE_USERNAME"),
                        DbConnectionDetailsManager.getDbConnectionDetails().get("DATABASE_PASSWORD")
                );
                connection.setAutoCommit(true);
                String uniqueReference = UUID.randomUUID().toString();
                connectionPool.add(new PooledConnection(connection, DbUtils.nowDbTimestamp(),uniqueReference));
                connectionPoolInUseStatuses.put(uniqueReference,false);
                maxConnectionsToOpen--;
            }
        }
    }

    protected static PooledConnection getConnection() {
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
