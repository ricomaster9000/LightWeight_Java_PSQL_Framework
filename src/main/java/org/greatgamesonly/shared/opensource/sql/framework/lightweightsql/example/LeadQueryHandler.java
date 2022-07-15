package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.example;


import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseBeanListHandler;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.RepositoryException;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

class LeadQueryHandler extends BaseBeanListHandler<Lead> {

    private final StatusTypeRepository statusTypeRepository;

    public LeadQueryHandler() throws IntrospectionException, IOException, InterruptedException {
        super(Lead.class);
        this.statusTypeRepository = new StatusTypeRepository();
    }

    @Override
    public List<Lead> handle(ResultSet rs) throws SQLException {
        return super.handle(rs);
    }
}
