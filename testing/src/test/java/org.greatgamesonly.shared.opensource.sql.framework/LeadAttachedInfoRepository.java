package org.greatgamesonly.shared.opensource.sql.framework;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.BaseRepository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.Repository;


@Repository(dbEntityClass = LeadAttachedInfo.class)
public class LeadAttachedInfoRepository extends BaseRepository<LeadAttachedInfo> {}
