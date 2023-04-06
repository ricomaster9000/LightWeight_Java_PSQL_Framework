package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.testing;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.Repository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.BaseRepository;


@Repository(dbEntityClass = StatusType.class, manyToOneCacheHours = 6/*default is 12*/)
public class StatusTypeRepository extends BaseRepository<StatusType> {}