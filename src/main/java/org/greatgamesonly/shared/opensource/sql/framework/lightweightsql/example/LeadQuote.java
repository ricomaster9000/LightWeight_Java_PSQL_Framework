package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.example;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.ColumnName;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.Entity;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.PrimaryKey;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseEntity;

import java.sql.Timestamp;

@Entity(tableName = "lead_quote", repositoryClass = LeadQuoteRepository.class)
public class LeadQuote extends BaseEntity {
    @PrimaryKey
    @ColumnName("id")
    private Long id;
    @ColumnName("quoteNo")
    private Long quoteNo;
    @ColumnName("lead_id")
    private Long leadId;
    @ColumnName("create_date")
    private Timestamp createDate;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuoteNo() {
        return quoteNo;
    }

    public void setQuoteNo(Long quoteNo) {
        this.quoteNo = quoteNo;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }
}
