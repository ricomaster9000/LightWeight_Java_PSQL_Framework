package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;

import java.util.HashMap;
import java.util.Map;

public class DbConnectionManager {

    protected static Map<String, String> CONNECTION_DETAILS = new HashMap<>();
    static final int DEFAULT_DB_CONNECTION_POOL_SIZE = 50;

    public static void setDbConnectionDetails(Map<String, String> connectionDetails) {
        CONNECTION_DETAILS = connectionDetails;
        if(connectionDetails.get("DB_CONNECTION_POOL_SIZE") == null) {
            CONNECTION_DETAILS.put("DB_CONNECTION_POOL_SIZE", String.valueOf(DEFAULT_DB_CONNECTION_POOL_SIZE));
        }
    }

    public static void setDbConnectionDetails(String databaseUrl, String username, String password) {
        CONNECTION_DETAILS.put("DatabaseUrl", databaseUrl);
        CONNECTION_DETAILS.put("User", username);
        CONNECTION_DETAILS.put("Password", password);
        CONNECTION_DETAILS.put("DB_CONNECTION_POOL_SIZE", String.valueOf(DEFAULT_DB_CONNECTION_POOL_SIZE));
    }

    public static void setDbConnectionDetails(String databaseUrl, String username, String password, Integer dbConnectionPoolSize) {
        CONNECTION_DETAILS.put("DatabaseUrl", databaseUrl);
        CONNECTION_DETAILS.put("User", username);
        CONNECTION_DETAILS.put("Password", password);
        CONNECTION_DETAILS.put("DB_CONNECTION_POOL_SIZE", dbConnectionPoolSize.toString());
    }

}
