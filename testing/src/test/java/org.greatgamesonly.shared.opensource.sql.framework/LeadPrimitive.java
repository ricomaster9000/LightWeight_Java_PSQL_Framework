package org.greatgamesonly.shared.opensource.sql.framework;



import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.BaseEntity;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "lead_primitive", repositoryClass = LeadPrimitiveRepository.class)
public class LeadPrimitive extends BaseEntity {
    @PrimaryKey
    @ColumnName("contact_id")
    protected long contactId;
    @ColumnName("external_reference_id")
    @DoNotUpdateInDb
    protected String externalReferenceId;
    @ColumnName("connex_id")
    @DoNotUpdateInDb
    protected long connexId;
    @ColumnName("ucid")
    @DoNotUpdateInDb
    protected String ucid;
    @ColumnName("create_date")
    @DoNotUpdateInDb
    protected Timestamp createDate;
    @ColumnName("modify_date")
    @ModifyDateAutoSet(timezone = "UTC")
    protected Timestamp modifyDate;
    @ColumnName("contact_type_id")
    @DoNotUpdateInDb
    protected long contactTypeId;
    @ColumnName("first_name")
    protected String firstName;
    @ColumnName("surname")
    protected String surname;
    @ColumnName("civil_reg_no")
    protected String civilRegNo;
    @ColumnName("phone_number")
    protected String phoneNumber;
    @ColumnName("email_address")
    protected String emailAddress;
    @ColumnName("product_id")
    protected long productId;
    @ColumnName("processing_id")
    protected String processingId;
    @OneToMany(referenceToColumnName = "lead_id", toManyEntityClass = LeadQuote.class, addToWherePartInGetQuery = "create_date >= now() - INTERVAL '365 DAY'") //do not ever worry about leadQuotes older than one year
    protected List<LeadQuote> leadQuotes;
    @OneToOne(referenceFromColumnName = "lead_attached_info_id", toOneEntityReferenceFromColumnName = "lead_id", toOneEntityClass = LeadAttachedInfo.class)
    private LeadAttachedInfo leadAttachedInfo;
    @OneToOneReferenceId(columnName = "lead_attached_info_id", referenceToColumnName = "id")
    private Long leadAttachedInfoId;
    @ColumnName("attached_pdf_document_data")
    private byte[] pdfDocumentData;
    @ManyToOne(linkedDbColumnName = "lead_type_id", toOneEntityClass = LeadType.class)
    private LeadType leadType;
    @ManyToOneReferenceId(columnName = "lead_type_id", referenceToColumnName = "id")
    private long leadTypeId;
    @DBIgnore
    protected Timestamp leadReceiveDate;
    @DBIgnore
    protected String leadProcessFailedReasonMsg;

    public LeadPrimitive() {}

    @Override
    public Long getId() {
        return contactId;
    }

    @Override
    public void setId(Long id) {
        this.contactId = id;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getExternalReferenceId() {
        return externalReferenceId;
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = externalReferenceId;
    }

    public long getConnexId() {
        return connexId;
    }

    public void setConnexId(long connexId) {
        this.connexId = connexId;
    }

    public String getUcid() {
        return ucid;
    }

    public void setUcid(String ucid) {
        this.ucid = ucid;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Long getContactTypeId() {
        return contactTypeId;
    }

    public void setContactTypeId(Long contactTypeId) {
        this.contactTypeId = contactTypeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getCivilRegNo() {
        return civilRegNo;
    }

    public void setCivilRegNo(String civilRegNo) {
        this.civilRegNo = civilRegNo;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProcessingId() {
        return processingId;
    }

    public void setProcessingId(String uniqueGUID) {
        this.processingId = uniqueGUID;
    }

    public LeadType getLeadType() {
        return leadType;
    }

    public void setLeadType(LeadType leadType) {
        this.leadType = leadType;
    }

    public long getLeadTypeId() {
        return leadTypeId;
    }

    public void setLeadTypeId(long leadTypeId) {
        this.leadTypeId = leadTypeId;
    }

    public Timestamp getLeadReceiveDate() {
        return leadReceiveDate;
    }

    public void setLeadReceiveDate(Timestamp leadReceiveDate) {
        this.leadReceiveDate = leadReceiveDate;
    }

    public String getLeadProcessFailedReasonMsg() {
        return leadProcessFailedReasonMsg;
    }

    public void setLeadProcessFailedReasonMsg(String leadProcessFailedReasonMsg) {
        this.leadProcessFailedReasonMsg = leadProcessFailedReasonMsg;
    }

    public Timestamp getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Timestamp modifyDate) {
        this.modifyDate = modifyDate;
    }

    public List<LeadQuote> getLeadQuotes() {
        if(leadQuotes == null) {
            this.leadQuotes = new ArrayList<>();
        }
        return leadQuotes;
    }

    public void setLeadQuotes(List<LeadQuote> leadQuotes) {
        this.leadQuotes = leadQuotes;
    }

    public LeadAttachedInfo getLeadAttachedInfo() {
        return leadAttachedInfo;
    }

    public void setLeadAttachedInfo(LeadAttachedInfo leadAttachedInfo) {
        this.leadAttachedInfo = leadAttachedInfo;
    }

    public Long getLeadAttachedInfoId() {
        return leadAttachedInfoId;
    }

    public void setLeadAttachedInfoId(Long leadAttachedInfoId) {
        this.leadAttachedInfoId = leadAttachedInfoId;
    }

    public byte[] getPdfDocumentData() {
        return pdfDocumentData;
    }

    public void setPdfDocumentData(byte[] pdfDocumentData) {
        this.pdfDocumentData = pdfDocumentData;
    }
}
