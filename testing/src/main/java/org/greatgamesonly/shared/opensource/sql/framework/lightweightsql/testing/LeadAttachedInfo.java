package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.testing;


import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.BaseEntity;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.*;

@Entity(tableName = "lead_attached_info", repositoryClass = LeadAttachedInfoRepository.class)
public class LeadAttachedInfo extends BaseEntity {
    @PrimaryKey
    @ColumnName("id")
    protected Long id;
    @ColumnName("lead_id")
    protected Long leadId;
    @ColumnName("info")
    protected String info;

    public LeadAttachedInfo() {}

    public LeadAttachedInfo(Long id, Long leadId, String info) {
        this.id = id;
        this.leadId = leadId;
        this.info = info;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
