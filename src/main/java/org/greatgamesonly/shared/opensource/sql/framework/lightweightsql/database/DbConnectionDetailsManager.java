package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database;

import org.greatgamesonly.opensource.utils.resourceutils.ResourceUtils;

import java.util.HashMap;
import java.util.Properties;

public class DbConnectionDetailsManager {

    private static final HashMap<String, String> CONNECTION_DETAILS = new HashMap<>();
    static final int DEFAULT_DB_CONNECTION_POOL_SIZE = 40;

    public static HashMap<String, String> getDbConnectionDetails() {
        if(CONNECTION_DETAILS.isEmpty()) {
            CONNECTION_DETAILS.put("DatabaseUrl", getDatabaseUrl());
            CONNECTION_DETAILS.put("User", getDatabaseUsername());
            CONNECTION_DETAILS.put("Password", getDatabasePassword());
            CONNECTION_DETAILS.put("DB_CONNECTION_POOL_SIZE", getDatabaseMaxDbConnectionPoolProperty());
        }
        return CONNECTION_DETAILS;
    }

    public static Integer getDatabaseMaxDbConnectionPool() {
        return Integer.parseInt(getDbConnectionDetails().get("DB_CONNECTION_POOL_SIZE"));
    }

    protected static String getDatabaseUrl() {
        String result = ResourceUtils.getProperty("datasource.url");
        if(result == null || result.isBlank()) {
            result = ResourceUtils.getProperty("quarkus.datasource.url");
        }
        if(result == null || result.isBlank()) {
            result = ResourceUtils.getProperty("DATABASE_URL");
        }
        return result;
    }

    protected static String getDatabaseUsername() {
        String result = ResourceUtils.getProperty("datasource.username");
        if(result == null || result.isBlank()) {
            result = ResourceUtils.getProperty("quarkus.datasource.username");
        }
        if(result == null || result.isBlank()) {
            result = ResourceUtils.getProperty("DATABASE_USERNAME");
        }
        return result;
    }

    protected static String getDatabasePassword() {
        String result = ResourceUtils.getProperty("datasource.password");
        if(result == null || result.isBlank()) {
            result = ResourceUtils.getProperty("quarkus.datasource.password");
        }
        if(result == null || result.isBlank()) {
            result = ResourceUtils.getProperty("DATABASE_PASSWORD");
        }
        return result;
    }

    protected static String getDatabaseMaxDbConnectionPoolProperty() {
        String result = ResourceUtils.getProperty("datasource.max_db_connection_pool_size");
        if(result == null || result.isBlank()) {
            result = ResourceUtils.getProperty("DB_CONNECTION_POOL_SIZE");
        }
        if(result == null || result.isBlank()) {
            result = String.valueOf(DEFAULT_DB_CONNECTION_POOL_SIZE);
        }
        return result;
    }

    public static void setProperties(Properties properties) {
        for (final String name: properties.stringPropertyNames())
            DbConnectionDetailsManager.CONNECTION_DETAILS.put(name, properties.getProperty(name));
    }

    protected static void setDatabaseMaxDbConnectionPoolSize(int newPoolSize) {
        CONNECTION_DETAILS.put("DB_CONNECTION_POOL_SIZE", String.valueOf(newPoolSize));
    }

}
