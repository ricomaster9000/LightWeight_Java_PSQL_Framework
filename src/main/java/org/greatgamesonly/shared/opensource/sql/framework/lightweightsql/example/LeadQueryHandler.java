package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.example;


import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseBeanListHandler;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.RepositoryException;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class LeadQueryHandler extends BaseBeanListHandler<Lead> {

    private final StatusTypeRepository statusTypeRepository;

    public LeadQueryHandler() throws IntrospectionException, IOException, InterruptedException {
        super(Lead.class);
        this.statusTypeRepository = new StatusTypeRepository();
    }

    @Override
    public List<Lead> handle(ResultSet rs) throws SQLException {
        List<Lead> leads = super.handle(rs);
        for(Lead lead : leads) {
            try {
                if(lead.getStatusId() != null) {
                    lead.setStatus(statusTypeRepository.getById(lead.getStatusId()).getName());
                }
            } catch (RepositoryException e) {
                throw new SQLException("Unable to get acceptedLeadMetadata: "+ e.getMessage(), e);
            }
        }
        return leads;
    }
}
