package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.example;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.Repository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseRepository;

import java.util.Map;


@Repository(dbEntityClass = LeadQuote.class)
class LeadQuoteRepository extends BaseRepository<LeadQuote> {}
