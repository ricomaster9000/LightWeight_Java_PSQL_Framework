package org.greatgamesonly.shared.opensource.sql.framework;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.BaseRepository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.Repository;

@Repository(dbEntityClass = LeadType.class, manyToOneCacheHours = 0)
public class LeadTypeRepository extends BaseRepository<LeadType> {
}
