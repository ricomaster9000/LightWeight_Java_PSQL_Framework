package org.greatgamesonly.shared.opensource.sql.framework;


import org.greatgamesonly.opensource.utils.resourceutils.ResourceUtils;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbConnectionDetailsManager;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.RepositoryException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;

public class MainTest {
    static StatusTypeRepository statusTypeRepository = null;
    static LeadRepository leadRepository = null;
    static LeadQuoteRepository leadQuoteRepository = null;
    static LeadAttachedInfoRepository leadAttachedInfoRepository = null;
    static LeadTypeRepository leadTypeRepository = null;


    @BeforeClass
    public static void setupEnvironment() {
        System.out.println("TESTS - Setting up repositories&connection-pools and establishing connection");
        Properties properties = new Properties();
        properties.put("DATABASE_URL",ResourceUtils.getProperty("DATABASE_URL"));
        properties.put("DATABASE_USERNAME",ResourceUtils.getProperty("DATABASE_USERNAME"));
        properties.put("DATABASE_PASSWORD",ResourceUtils.getProperty("DATABASE_PASSWORD"));
        properties.put("DB_CONNECTION_POOL_SIZE","5");
        DbConnectionDetailsManager.setProperties(properties);

        leadRepository = new LeadRepository();
        leadQuoteRepository = new LeadQuoteRepository();
        statusTypeRepository = new StatusTypeRepository();
        leadAttachedInfoRepository = new LeadAttachedInfoRepository();
        leadTypeRepository = new LeadTypeRepository();
        System.out.println("TESTS - BEGIN");
    }

    @Test()
    public void testCreateNewEntities() throws RepositoryException {
        System.out.println("TESTS - Creating new entities and inserting these entities into database");
        Lead leadTest1 = new Lead();
        leadTest1.setContactId(1L);
        leadTest1.setExternalReferenceId("TestExternalReference");
        leadTest1.setConnexId(null);
        leadTest1.setUcid("TEST_UCID");
        leadTest1.setCreateDate(nowDbTimestamp());
        leadTest1.setFirstName("test_firstname");
        leadTest1.setSurname("test_surname");
        leadTest1.setCivilRegNo("05055555131342");
        leadTest1.setPhoneNumber("0123456789");
        leadTest1.setEmailAddress("testing@gmail.com");
        leadTest1.setProductId(3L);
        leadTest1.setProcessingId(UUID.randomUUID().toString());
        leadTest1.setLeadReceiveDate(nowDbTimestamp());

        Lead leadTest2 = new Lead();
        leadTest2.setExternalReferenceId(UUID.randomUUID().toString());
        leadTest2.setConnexId(null);
        leadTest2.setUcid("TEST_UCID");
        leadTest2.setCreateDate(nowDbTimestamp());
        leadTest2.setFirstName("test_firstname");
        leadTest2.setSurname("test_surname");
        leadTest2.setCivilRegNo("05085555131342");
        leadTest2.setPhoneNumber("0123456789");
        leadTest2.setEmailAddress("testing@gmail.com");
        leadTest2.setProductId(3L);
        leadTest2.setProcessingId(UUID.randomUUID().toString());
        leadTest2.setLeadReceiveDate(nowDbTimestamp());

        Lead leadTest3 = new Lead();
        leadTest3.setExternalReferenceId(UUID.randomUUID().toString());
        leadTest3.setConnexId(null);
        leadTest3.setUcid("PRINGO");
        leadTest3.setCreateDate(nowDbTimestamp());
        leadTest3.setFirstName("test_firstname");
        leadTest3.setSurname("test_surname");
        leadTest3.setCivilRegNo("05055555145342");
        leadTest3.setPhoneNumber("0123456789");
        leadTest3.setEmailAddress("testing@gmail.com");
        leadTest3.setProductId(3L);
        leadTest3.setProcessingId(UUID.randomUUID().toString());
        leadTest3.setLeadReceiveDate(nowDbTimestamp());
        leadTest3.setPdfDocumentData("pdfFileData".getBytes());

        Lead leadTest4 = new Lead();
        leadTest4.setExternalReferenceId(UUID.randomUUID().toString());
        leadTest4.setConnexId(null);
        leadTest4.setUcid("MANGO");
        leadTest4.setCreateDate(nowDbTimestamp());
        leadTest4.setFirstName("test_firstname");
        leadTest4.setSurname("test_surname");
        leadTest4.setCivilRegNo("05025555131342");
        leadTest4.setPhoneNumber("0123456789");
        leadTest4.setEmailAddress("testing@gmail.com");
        leadTest4.setProductId(5L);
        leadTest4.setProcessingId(UUID.randomUUID().toString());
        leadTest4.setLeadReceiveDate(nowDbTimestamp());

        Lead leadTest5 = new Lead();
        leadTest5.setExternalReferenceId(UUID.randomUUID().toString());
        leadTest5.setConnexId(null);
        leadTest5.setUcid("DECKO");
        leadTest5.setCreateDate(nowDbTimestamp());
        leadTest5.setFirstName("test_firstname");
        leadTest5.setSurname("test_surname");
        leadTest5.setCivilRegNo("11055555131342");
        leadTest5.setPhoneNumber("0123456789");
        leadTest5.setEmailAddress("testing@gmail.com");
        leadTest5.setProductId(6L);
        leadTest5.setProcessingId(UUID.randomUUID().toString());
        leadTest5.setLeadReceiveDate(nowDbTimestamp());

        Assert.assertTrue("insertEntities - leads must be created and contain correct data and have primary keys assigned",
                leadRepository.insertEntities(List.of(leadTest1,leadTest2,leadTest3,leadTest4)).stream()
                        .allMatch(l ->
                                l.getId() > 0L &&
                                l.getUcid() != null && !l.getUcid().isBlank() &&
                                l.getCreateDate() != null &&
                                l.getFirstName() != null && !l.getFirstName().isBlank() &&
                                l.getSurname() != null && !l.getSurname().isBlank() &&
                                l.getCivilRegNo() != null && !l.getCivilRegNo().isBlank() &&
                                l.getPhoneNumber() != null && !l.getPhoneNumber().isBlank() &&
                                l.getEmailAddress() != null && !l.getEmailAddress().isBlank() &&
                                l.getProductId() > 0L &&
                                l.getLeadReceiveDate() != null /*@DbIgnore annotations must be respected*/));

        Assert.assertTrue("insertOrUpdate - leads must be created and contain correct data and have primary keys assigned",
                Stream.of(leadRepository.insertOrUpdate(leadTest5))
                        .allMatch(l ->
                                l.getId() > 0L &&
                                l.getUcid() != null && !l.getUcid().isBlank() &&
                                l.getCreateDate() != null &&
                                "test_firstname".equals(l.getFirstName()) &&
                                l.getSurname() != null && !l.getSurname().isBlank() &&
                                l.getCivilRegNo() != null && !l.getCivilRegNo().isBlank() &&
                                l.getPhoneNumber() != null && !l.getPhoneNumber().isBlank() &&
                                l.getEmailAddress() != null && !l.getEmailAddress().isBlank() &&
                                6L == l.getProductId() &&
                                l.getLeadReceiveDate() != null /*@DbIgnore annotations must be respected*/));
    }

    @Test()
    public void testByteArrayFieldSupport() throws RepositoryException {
        System.out.println("TESTS - Fetching entity with byte array data and checking if its still valid");

        final String pdfFileData = "pdfFileData";

        Lead leadTest = new Lead();
        leadTest.setExternalReferenceId(UUID.randomUUID().toString());
        leadTest.setConnexId(null);
        leadTest.setUcid("PRINGO");
        leadTest.setCreateDate(nowDbTimestamp());
        leadTest.setFirstName("test_firstname");
        leadTest.setSurname("test_surname");
        leadTest.setCivilRegNo("05055555145342");
        leadTest.setPhoneNumber("0123456789");
        leadTest.setEmailAddress("testing@gmail.com");
        leadTest.setProductId(3L);
        leadTest.setProcessingId(UUID.randomUUID().toString());
        leadTest.setLeadReceiveDate(nowDbTimestamp());
        leadTest.setPdfDocumentData(pdfFileData.getBytes());

        leadRepository.insertEntities(leadTest);

        List<Lead> leadsWithByteData = leadRepository.getByColumnIfNotNull("attached_pdf_document_data");
        Assert.assertTrue(
                "no entities with byteData could be fetched even though entities were persisted with byte data",
                !leadsWithByteData.isEmpty() && leadsWithByteData.stream().allMatch(l -> l.getPdfDocumentData() != null)
        );

        Assert.assertTrue(
                "entity fetched does not have correct byte array data in the correct format",
                leadsWithByteData.stream().anyMatch(l -> new String(l.getPdfDocumentData()).equals(pdfFileData))
        );
    }

    @Test()
    public void testUpdateEntities() throws RepositoryException {
        System.out.println("TESTS - Fetching newly created entities and updating those entities and persisting changes");

        List<Lead> leadsFetched = leadRepository.getAll();

        for(Lead lead : leadsFetched) {
            String oldProcessingId = lead.getProcessingId();
            lead.setProcessingId(UUID.randomUUID().toString()+"1");
            Lead updatedLead = leadRepository.insertOrUpdate(lead);

            Assert.assertNotEquals("entity updated has the same data after changing it and persisting to database",
                    oldProcessingId, updatedLead.getProcessingId());
        }
    }

    @Test()
    public void testCreatingOneToManyRelations() throws RepositoryException {
        System.out.println("TESTS - adding oneToMany relationships then persisting changes");

        String externalReferenceId = UUID.randomUUID().toString();

        Lead leadTestOneToManyInsert = new Lead();
        leadTestOneToManyInsert.setExternalReferenceId(externalReferenceId);
        leadTestOneToManyInsert.setConnexId(null);
        leadTestOneToManyInsert.setUcid("DECKOS");
        leadTestOneToManyInsert.setCreateDate(nowDbTimestamp());
        leadTestOneToManyInsert.setFirstName("test_firstname55");
        leadTestOneToManyInsert.setSurname("test_surname55");
        leadTestOneToManyInsert.setCivilRegNo("11055555131342");
        leadTestOneToManyInsert.setPhoneNumber("0123456782");
        leadTestOneToManyInsert.setEmailAddress("testing@gmail.com");
        leadTestOneToManyInsert.setProductId(6L);
        leadTestOneToManyInsert.setProcessingId(UUID.randomUUID().toString());
        leadTestOneToManyInsert.setLeadReceiveDate(nowDbTimestamp());
        leadTestOneToManyInsert.getLeadQuotes().add(new LeadQuote(2342342L));

        Assert.assertTrue("entity with one to many relations created and returns created one to many relations",
                Stream.of(leadRepository.insertEntity(leadTestOneToManyInsert)).allMatch(l ->
                        l.getId() > 0L &&
                        !l.getLeadQuotes().isEmpty() &&
                        l.getLeadQuotes().get(0).getId() > 0L)
        );
    }

    @Test()
    public void testCreatingOneToOneRelations() throws RepositoryException {
        System.out.println("TESTS - adding oneToOne relationships then persisting changes");

        String referenceId = "testy543dfgdfg45645445";
        Lead leadTestOneToOneInsert = new Lead();
        leadTestOneToOneInsert.setExternalReferenceId(referenceId);
        leadTestOneToOneInsert.setConnexId(null);
        leadTestOneToOneInsert.setUcid("DECKOS");
        leadTestOneToOneInsert.setCreateDate(nowDbTimestamp());
        leadTestOneToOneInsert.setFirstName("test_firstname55");
        leadTestOneToOneInsert.setSurname("test_surname55");
        leadTestOneToOneInsert.setCivilRegNo("11055555131342");
        leadTestOneToOneInsert.setPhoneNumber("0123456782");
        leadTestOneToOneInsert.setEmailAddress("testing@gmail.com");
        leadTestOneToOneInsert.setProductId(6L);
        leadTestOneToOneInsert.setProcessingId(UUID.randomUUID().toString());
        leadTestOneToOneInsert.setLeadReceiveDate(nowDbTimestamp());
        leadTestOneToOneInsert.getLeadQuotes().add(new LeadQuote(23423424L));
        leadRepository.insertEntities(leadTestOneToOneInsert);

        Lead leadFetched = leadRepository.getByColumn("external_reference_id",referenceId).get(0);
        LeadAttachedInfo testAttachedLeadInfo = new LeadAttachedInfo(1L, leadFetched.getId(), "test_status");
        leadFetched.setLeadAttachedInfo(leadAttachedInfoRepository.insertEntity(testAttachedLeadInfo));
        Lead updatedOneToOneLead = leadRepository.insertOrUpdate(leadFetched);
        updatedOneToOneLead = leadRepository.getById(updatedOneToOneLead.getId());

        Assert.assertTrue("entity updated does not have one to one entity attached to it",
                updatedOneToOneLead.getId() > 0L &&
                updatedOneToOneLead.getLeadAttachedInfo() != null &&
                updatedOneToOneLead.getLeadAttachedInfo().getId() > 0L
        );
    }

    @Test()
    public void testRemovingOneToOneRelations() throws RepositoryException {
        System.out.println("TESTS - fetching entities to remove oneToOne relationships and persisting changes");

        String referenceId = "testy5435445";
        Lead leadTestOneToOneInsert = new Lead();
        leadTestOneToOneInsert.setExternalReferenceId(referenceId);
        leadTestOneToOneInsert.setConnexId(null);
        leadTestOneToOneInsert.setUcid("DECKOS");
        leadTestOneToOneInsert.setCreateDate(nowDbTimestamp());
        leadTestOneToOneInsert.setFirstName("test_firstname55");
        leadTestOneToOneInsert.setSurname("test_surname55");
        leadTestOneToOneInsert.setCivilRegNo("11055555131342");
        leadTestOneToOneInsert.setPhoneNumber("0123456782");
        leadTestOneToOneInsert.setEmailAddress("testing@gmail.com");
        leadTestOneToOneInsert.setProductId(6L);
        leadTestOneToOneInsert.setProcessingId(UUID.randomUUID().toString());
        leadTestOneToOneInsert.setLeadReceiveDate(nowDbTimestamp());
        leadTestOneToOneInsert.getLeadQuotes().add(new LeadQuote(234234777724L));
        leadRepository.insertEntities(leadTestOneToOneInsert);

        Lead leadFetched = leadRepository.getByColumn("external_reference_id", referenceId).get(0);
        LeadAttachedInfo testAttachedLeadInfo = new LeadAttachedInfo(1L, leadFetched.getId(), "test_status345345");
        leadFetched.setLeadAttachedInfo(leadAttachedInfoRepository.insertEntity(testAttachedLeadInfo));
        Lead updatedOneToOneLead = leadRepository.insertOrUpdate(leadFetched);
        Lead leadToRemoveOneToOne = leadRepository.getById(updatedOneToOneLead.getId());
        leadToRemoveOneToOne.setLeadAttachedInfo(null);
        leadRepository.updateEntities(leadToRemoveOneToOne);
        leadToRemoveOneToOne = leadRepository.getById(leadToRemoveOneToOne.getId());

        Assert.assertTrue("entity one to one relation was not deleted",
                leadToRemoveOneToOne.getLeadAttachedInfo() == null &&
                leadAttachedInfoRepository.countByColumn("info","test_status345345") == 0
        );
    }

    @Test()
    public void testManyToOneEntitiesCreate() throws RepositoryException {
        System.out.println("TESTS - Creating new entities for ManyToOneRelationships and inserting these entities into the database");

        StatusType testStatusType = new StatusType("test_status");
        statusTypeRepository.insertEntities(testStatusType);

        LeadType testLeadType = new LeadType();
        testLeadType.setName("Test_type99999900000");
        testLeadType = leadTypeRepository.insertEntities(testLeadType).get(0);

        String referenceId = "testy543dfgdfgfghdfh4454545nnnnnnnnn";
        Lead leadTestOneToOneInsert = new Lead();
        leadTestOneToOneInsert.setExternalReferenceId(referenceId);
        leadTestOneToOneInsert.setConnexId(null);
        leadTestOneToOneInsert.setUcid("DECKOS");
        leadTestOneToOneInsert.setCreateDate(nowDbTimestamp());
        leadTestOneToOneInsert.setFirstName("test_firstname55");
        leadTestOneToOneInsert.setSurname("test_surname55");
        leadTestOneToOneInsert.setCivilRegNo("11055555131342");
        leadTestOneToOneInsert.setPhoneNumber("0123456782");
        leadTestOneToOneInsert.setEmailAddress("testing@gmail.com");
        leadTestOneToOneInsert.setProductId(6L);
        leadTestOneToOneInsert.setProcessingId(UUID.randomUUID().toString());
        leadTestOneToOneInsert.setLeadReceiveDate(nowDbTimestamp());
        leadTestOneToOneInsert.setLeadType(testLeadType);
        leadRepository.insertEntities(leadTestOneToOneInsert);

        Lead leadFetched = leadRepository.getByColumn("external_reference_id",referenceId).get(0);

        Assert.assertTrue("entity updated does not have many to one entity attached to it",
                leadFetched.getId() > 0L && leadFetched.getLeadType() != null && leadFetched.getLeadType().getId() > 0L
        );
    }

    @Test
    public void testGetByColumn() throws Exception {
        System.out.println("TESTS - Fetching entities in a filtered way");

        String statusTypeName1 = "test_status_50";
        String statusTypeName2 = "test_status_51";
        statusTypeRepository.insertEntities(new StatusType(statusTypeName1),new StatusType(statusTypeName2));

        Assert.assertTrue("entity must be retrieved using filtered get methods",
            statusTypeRepository.getByColumn("name",statusTypeName2) != null &&
            statusTypeRepository.getByColumnIfNotNull("name") != null &&
            statusTypeRepository.getByColumnGreaterAs("id",0L).size() >= 2
        );
    }

    @AfterClass
    public static void PostTestClassRun() {
        System.out.println("TESTS - CLEAN UP DATA");
        if(leadRepository != null) {
            leadRepository.deleteAllNoException();
        }
        if(leadQuoteRepository != null) {
            leadQuoteRepository.deleteAllNoException();
        }
        if(statusTypeRepository != null) {
            statusTypeRepository.deleteAllNoException();
        }
        if(leadAttachedInfoRepository != null) {
            leadAttachedInfoRepository.deleteAllNoException();
        }
        if(leadTypeRepository != null) {
            leadTypeRepository.deleteAllNoException();
        }
        System.out.println("TESTS - END");
    }

    public static Calendar nowCal() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
        return calendar;
    }

    public static Date now() {
        return nowCal().getTime();
    }

    public static java.sql.Timestamp nowDbTimestamp() {
        return new java.sql.Timestamp(nowCal().getTimeInMillis());
    }

}
