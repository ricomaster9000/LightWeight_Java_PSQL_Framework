package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.example;

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
    @ColumnName("quoteNo")
    private Long quoteNo;
    @ColumnName("lead_id")
    private Long leadId;
    @ColumnName("create_date")
    private Timestamp createDate;
    @ColumnName("quote_document_data")
    protected byte[] quoteDocumentData;
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

    public byte[] getQuoteDocumentData() {
        return quoteDocumentData;
    }
    public void setQuoteDocumentData(byte[] quoteDocumentData) {
        this.quoteDocumentData = quoteDocumentData;
    }
}
