package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.example;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.Repository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseRepository;

import java.util.Map;


@Repository(dbEntityClass = StatusType.class, manyToOneCacheHours = 6/*default is 12*/)
class StatusTypeRepository extends BaseRepository<StatusType> {}
