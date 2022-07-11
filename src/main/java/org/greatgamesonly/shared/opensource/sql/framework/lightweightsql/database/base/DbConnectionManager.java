package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;

import java.util.HashMap;
import java.util.Map;

public class DbConnectionManager {

    protected static Map<String, String> CONNECTION_DETAILS = new HashMap<>();
    private static int inMemoryCacheHoursForManyToOne = 12;

    public static void setDbConnectionDetails(Map<String, String> connectionDetails) {
        CONNECTION_DETAILS = connectionDetails;
    }

    public static void setDbConnectionDetails(String databaseUrl, String username, String password) {
        CONNECTION_DETAILS.put("DatabaseUrl", databaseUrl);
        CONNECTION_DETAILS.put("User", username);
        CONNECTION_DETAILS.put("Password", password);
    }

    public static int getInMemoryCacheHoursForManyToOne() {
        return inMemoryCacheHoursForManyToOne;
    }

}
