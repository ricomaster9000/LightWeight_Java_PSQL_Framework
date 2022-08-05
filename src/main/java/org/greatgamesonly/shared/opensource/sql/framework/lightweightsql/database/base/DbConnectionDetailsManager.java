package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;

import java.util.HashMap;
import java.util.Properties;

public class DbConnectionDetailsManager {

    private static final HashMap<String, String> CONNECTION_DETAILS = new HashMap<>();
    static final int DEFAULT_DB_CONNECTION_POOL_SIZE = 40;

    private static Properties properties;

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

    protected static String getConfigurationProperty(String keyName) {
        String result = getProperties().getProperty(keyName);
        if(result == null || result.isBlank()) {
            result = System.getenv(keyName);
        }
        return result;
    }

    protected static String getDatabaseUrl() {
        String result = getConfigurationProperty("datasource.url");
        if(result == null || result.isBlank()) {
            result = getConfigurationProperty("quarkus.datasource.url");
        }
        if(result == null || result.isBlank()) {
            result = getConfigurationProperty("DATABASE_URL");
        }
        return result;
    }

    protected static String getDatabaseUsername() {
        String result = getConfigurationProperty("datasource.username");
        if(result == null || result.isBlank()) {
            result = getConfigurationProperty("quarkus.datasource.username");
        }
        if(result == null || result.isBlank()) {
            result = getConfigurationProperty("DATABASE_USERNAME");
        }
        return result;
    }

    protected static String getDatabasePassword() {
        String result = getConfigurationProperty("datasource.password");
        if(result == null || result.isBlank()) {
            result = getConfigurationProperty("quarkus.datasource.password");
        }
        if(result == null || result.isBlank()) {
            result = getConfigurationProperty("DATABASE_PASSWORD");
        }
        return result;
    }

    protected static String getDatabaseMaxDbConnectionPoolProperty() {
        String result = getConfigurationProperty("DB_CONNECTION_POOL_SIZE");
        if(result == null || result.isBlank()) {
            result = String.valueOf(DEFAULT_DB_CONNECTION_POOL_SIZE);
        }
        return result;
    }

    public static Properties loadPropertiesFile() {
        Properties result = new Properties();
        try {
            result.load(getContextClassLoader().getResourceAsStream("config.properties"));
        } catch (Exception ignore) {}
        if(result.isEmpty()) {
            try {
                result.load(getContextClassLoader().getResourceAsStream("application.properties"));
            } catch (Exception ignore) {}
        }
        if(result.isEmpty()) {
            throw new RuntimeException("Unable to pull properties file, check if you have one set");
        }
        return result;
    }

    protected static Properties getProperties() {
        if(properties == null || properties.isEmpty()) {
            properties = loadPropertiesFile();
        }
        return properties;
    }

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
