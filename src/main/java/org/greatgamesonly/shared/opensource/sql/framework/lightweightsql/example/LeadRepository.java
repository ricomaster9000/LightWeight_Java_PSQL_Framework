package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.example;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseRepository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.RepositoryException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


class LeadRepository extends BaseRepository<Lead> {

    public LeadRepository() {}

    @Override
    public Class<Lead> getDbEntityClass() {
        return Lead.class;
    }

    @Override
    public Map<String, String> getDbConnectionDetails() {
        return Map.of("DatabaseName", "testDbName",
                "User", "TestUser",
                "Password", "TestPassword");
    }

    public Lead getByConnexId(Long connexId) throws RepositoryException {
        List<Lead> leads = super.executeGetQuery("SELECT * from leads WHERE connex_id = ?", connexId);
        return leads != null && leads.size() > 0 ? leads.get(0) : null;
    }

    public Lead getByPhoneNumber(String phoneNumber) throws RepositoryException {
        return super.getByField("phone_number", phoneNumber);
    }

    public Lead getByPhoneNumberOldestFirst(String phoneNumber) throws RepositoryException {
        return super.getByFieldOrderByPrimaryKey("phone_number", phoneNumber, OrderBy.ASC);
    }

    public List<Lead> getAllAfterCreateDateRawQuery(java.sql.Timestamp afterThisDate) throws RepositoryException, SQLException {
        List<Lead> result = new ArrayList<>();
        ResultSet resultSet = super.executeGetQueryRaw("SELECT * from leads WHERE create_date > '"+afterThisDate.toString()+
                "' ORDER BY create_date DESC;");
        while (resultSet.next()) {
            Lead entityToReturn = new Lead();
            entityToReturn.setStatusId(resultSet.getLong("status_id"));
            entityToReturn.setConnexId(resultSet.getLong("connex_id"));
            entityToReturn.setCivilRegNo(resultSet.getString("civil_reg_no"));
            entityToReturn.setContactTypeId(resultSet.getLong("contact_type_id"));
            entityToReturn.setCreateDate(resultSet.getTimestamp("create_date"));
            entityToReturn.setProductId(resultSet.getLong("product_id"));
            entityToReturn.setContactId(resultSet.getLong("contact_id"));
            entityToReturn.setEmailAddress(resultSet.getString("email_address"));
            entityToReturn.setExternalReferenceId(resultSet.getString("external_reference_id"));
            entityToReturn.setFirstName(resultSet.getString("first_name"));
            entityToReturn.setSurname(resultSet.getString("surname"));
            entityToReturn.setPhoneNumber(resultSet.getString("phone_number"));
            entityToReturn.setUcid(resultSet.getString("ucid"));
            entityToReturn.setProcessingId(resultSet.getString("processing_id"));
            result.add(entityToReturn);
        }
        resultSet.close();
        return result;
    }

    // The override is not needed, its just to show you this method exists in base repository, it automatically updates entities accordingly
    // It takes into account the DoNotUpdateInDb and PrimaryKey annotations
    @Override
    public Lead insertOrUpdate(Lead entity) throws RepositoryException {
        return super.insertOrUpdate(entity);
    }

    public Lead insertOrUpdateCustom(Lead entity) throws RepositoryException {
        Lead lead = entity.getContactId() != null ? getById(entity.getContactId()) : null;
        if(lead == null) {
            lead = insertEntities(entity).get(0);
            //Here you can insert sub entities linked to this entity for example
        } else {
            lead.setStatusId(entity.getStatusId());
            lead.setUcid(entity.getUcid());
            lead.setContactTypeId(entity.getContactTypeId());
            lead.setFirstName(entity.getFirstName());
            lead.setSurname(entity.getSurname());
            lead.setPhoneNumber(entity.getPhoneNumber());
            lead.setCivilRegNo(entity.getCivilRegNo());
            lead.setConnexId(entity.getConnexId());
            lead = updateEntities(lead).get(0);
        }
        return lead;
    }
}
