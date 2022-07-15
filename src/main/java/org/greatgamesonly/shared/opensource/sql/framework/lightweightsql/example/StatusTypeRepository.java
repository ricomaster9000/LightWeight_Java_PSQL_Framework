package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.example;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.Repository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseRepository;

import java.util.Map;


@Repository(dbEntityClass = StatusType.class)
class StatusTypeRepository extends BaseRepository<StatusType> {

    @Override
    public Map<String, String> getDbConnectionDetails() {
        return Map.of("DatabaseUrl", "testDbName",
                "User", "TestUser",
                "Password", "TestPassword");
    }
}
