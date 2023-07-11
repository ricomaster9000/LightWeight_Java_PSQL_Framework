package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.testing;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.BaseRepository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.Repository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.RepositoryException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository(dbEntityClass = LeadType.class, manyToOneCacheHours = 0)
public class LeadTypeRepository extends BaseRepository<LeadType> {
}
