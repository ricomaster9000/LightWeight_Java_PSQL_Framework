package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;

import java.util.HashMap;
import java.util.Map;

public class DbConnectionManager {

    protected static Map<String, String> CONNECTION_DETAILS = new HashMap<>();

    public static void setDbConnectionDetails(Map<String, String> connectionDetails) throws Exception {
        CONNECTION_DETAILS = connectionDetails;
    }

    public static void setDbConnectionDetails(String databaseUrl, String username, String password) throws Exception {
        CONNECTION_DETAILS.put("DatabaseUrl", databaseUrl);
        CONNECTION_DETAILS.put("User", username);
        CONNECTION_DETAILS.put("Password", password);
    }
}
