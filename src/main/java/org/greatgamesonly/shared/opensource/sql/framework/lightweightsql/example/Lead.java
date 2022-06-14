package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.example;


import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.ColumnName;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DBIgnore;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.PrimaryKey;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.TableName;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseEntity;

import java.sql.Timestamp;

@TableName("lead")
public class Lead extends BaseEntity {
    @PrimaryKey
    @ColumnName("contact_id")
    protected Long contactId;
    @ColumnName("external_reference_id")
    protected String externalReferenceId;
    @ColumnName("connex_id")
    protected Long connexId;
    @ColumnName("ucid")
    protected String ucid;
    @ColumnName("create_date")
    protected Timestamp createDate;
    @ColumnName("status_id")
    protected Long statusId;
    @DBIgnore
    protected String status;
    @ColumnName("contact_type_id")
    protected Long contactTypeId;
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
    protected Long productId;
    @ColumnName("processing_id")
    protected String processingId;
    @DBIgnore
    protected Timestamp leadReceiveDate;
    @DBIgnore
    protected String leadProcessFailedReasonMsg;

    public Lead() {}

    @Override
    public java.lang.Long getId() {
        return contactId;
    }

    @Override
    public void setId(Long id) {
        this.contactId = id;
    }

    public java.lang.Long getContactId() {
        return contactId;
    }

    public void setContactId(java.lang.Long contactId) {
        this.contactId = contactId;
    }

    public String getExternalReferenceId() {
        return externalReferenceId;
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = externalReferenceId;
    }

    public java.lang.Long getConnexId() {
        return connexId;
    }

    public void setConnexId(java.lang.Long connexId) {
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

    public java.lang.Long getStatusId() {
        return statusId;
    }

    public void setStatusId(java.lang.Long statusId) {
        this.statusId = statusId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.lang.Long getContactTypeId() {
        return contactTypeId;
    }

    public void setContactTypeId(java.lang.Long contactTypeId) {
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

    public java.lang.Long getProductId() {
        return productId;
    }

    public void setProductId(java.lang.Long productId) {
        this.productId = productId;
    }

    public String getProcessingId() {
        return processingId;
    }

    public void setProcessingId(String uniqueGUID) {
        this.processingId = uniqueGUID;
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
}
