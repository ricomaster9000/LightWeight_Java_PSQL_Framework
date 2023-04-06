package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.testing;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.Repository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.BaseRepository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.RepositoryException;

import java.util.List;


@Repository(dbEntityClass = LeadQuote.class)
public class LeadQuoteRepository extends BaseRepository<LeadQuote> {

    @Override
    public List<LeadQuote> insertEntities(List<LeadQuote> entitiesToInsert) throws RepositoryException {
        // add some code you want to here
        return super.insertEntities(entitiesToInsert);
    }
}
