package org.greatgamesonly.shared.opensource.sql.framework;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.ColumnName;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.Entity;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.PrimaryKey;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.BaseEntity;

import java.sql.Timestamp;

@Entity(tableName = "lead_quote", repositoryClass = LeadQuoteRepository.class)
public class LeadQuote extends BaseEntity {
    @PrimaryKey
    @ColumnName("id")
    private Long id;
    @ColumnName("quote_no")
    private Long quoteNo;
    @ColumnName("lead_id")
    private Long leadId;
    @ColumnName("create_date")
    private Timestamp createDate = MainTest.nowDbTimestamp();

    public LeadQuote() {}

    public LeadQuote(Long quoteNo) {
        this.quoteNo = quoteNo;
    }

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
