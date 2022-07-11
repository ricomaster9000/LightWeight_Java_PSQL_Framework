package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.example;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.DbConnectionManager;

import java.util.Map;
import java.util.Properties;

public class StartupAppMethodClass {

    void startupMethodExample(Properties properties) {
        DbConnectionManager.setDbConnectionDetails(Map.of(
                "DatabaseUrl",properties.getProperty("DATABASE_URL"),
                "User",properties.getProperty("DATABASE_USERNAME"),
                "Password",properties.getProperty("DATABASE_PASSWORD")));


    }
}
