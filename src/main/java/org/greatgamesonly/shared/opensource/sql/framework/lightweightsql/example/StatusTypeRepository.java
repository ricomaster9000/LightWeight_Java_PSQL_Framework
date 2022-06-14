package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.example;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseRepository;

import java.util.Map;


public class StatusTypeRepository extends BaseRepository<StatusType> {
    @Override
    protected Class<StatusType> getDbEntityClass() {
        return StatusType.class;
    }

    @Override
    public Map<String, String> getDbConnectionDetails() {
        return Map.of("DatabaseName", "testDbName",
                "User", "TestUser",
                "Password", "TestPassword");
    }
}
