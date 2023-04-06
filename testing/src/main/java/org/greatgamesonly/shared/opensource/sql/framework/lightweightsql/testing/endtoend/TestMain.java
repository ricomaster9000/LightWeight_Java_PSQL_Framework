package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.testing.endtoend;



import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbConnectionDetailsManager;

import java.util.*;

import static java.time.ZoneOffset.UTC;

public class TestMain {

    public static void main(String[] args) {
        doTests();
        System.exit(0);
    }

    private static void doTests() {
        System.out.println("TESTS - BEGIN");
        StatusTypeRepository statusTypeRepository = null;
        LeadRepository leadRepository = null;
        LeadQuoteRepository leadQuoteRepository = null;

        String pdfDocumentData = "hahahahahahahahaha";

        try {
            System.out.println("TESTS - Setting up repositories&connection-pools and establishing connection");

            Properties properties = new Properties();
            properties.put("DATABASE_URL","jdbc:postgresql://testdatabase.cmw5kvj6zl6u.eu-west-1.rds.amazonaws.com:5432/test_sql_framework");
            properties.put("DATABASE_USERNAME","sqlframeworkTest");
            properties.put("DATABASE_PASSWORD","Masdadasd2234234___hadfhajsd_j");
            properties.put("DB_CONNECTION_POOL_SIZE","2");
            DbConnectionDetailsManager.setProperties(properties);

            leadRepository = new LeadRepository();
            leadQuoteRepository = new LeadQuoteRepository();
            statusTypeRepository = new StatusTypeRepository();

            System.out.println("TESTS - Creating new entities and inserting these entities into database");

            Lead leadTest1 = new Lead();
            leadTest1.setContactId(1L);
            leadTest1.setExternalReferenceId("GHSJHKJHHSIDG");
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
            leadTest2.setContactId(5L);
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
            leadTest3.setContactId(7L);
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
            leadTest3.setPdfDocumentData(pdfDocumentData.getBytes());

            Lead leadTest4 = new Lead();
            leadTest4.setContactId(11L);
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
            leadTest5.setContactId(3453L);
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

            leadRepository.insertEntities(List.of(leadTest1,leadTest2,leadTest3,leadTest4,leadTest5));

            System.out.println("TESTS - Fetching entity with byte array data and checking if its still valid");
            List<Lead> leadsWithByteData = leadRepository.getByColumnIfNotNull("attached_pdf_document_data");
            if(leadsWithByteData.isEmpty()) {
                throw new java.lang.RuntimeException("no entities with byteData could be fetched even though entities were persisted with byte data");
            }
            for(Lead lead : leadsWithByteData) {
                if(lead.getContactId() == 7L && !new String(lead.getPdfDocumentData()).equals(pdfDocumentData)) {
                    throw new java.lang.RuntimeException("entity fetched does not have correct byte array data in the correct format");
                }
            }

            System.out.println("TESTS - Fetching newly created entities and updating those entities and persisting changes");

            List<Lead> leadsFetched = leadRepository.getAll();

            for(Lead lead : leadsFetched) {
                String oldProcessingId = lead.getProcessingId();
                lead.setProcessingId(UUID.randomUUID().toString()+"1");
                Lead updatedLead = leadRepository.insertOrUpdate(lead);
                if(oldProcessingId.equals(updatedLead.getProcessingId())) {
                    throw new java.lang.RuntimeException("entity updated has the same data after changing it and persisting to database");
                }
            }

            System.out.println("TESTS - Fetching entities in a filtered way");

            System.out.println("TESTS - Fetching updated entities and adding oneToMany relationships then persisting changes");

            //lead.setLeadQuotes(List<LeadQuote> leadQuotes)

            System.out.println("TESTS - Fetching updated entities with oneToMany relationships");

            System.out.println("TESTS - Creating new entities for ManyToOneRelationships and inserting these entities into the database");

            StatusType testStatusType = new StatusType(1L,"test_status");
            statusTypeRepository.insertEntities(testStatusType);

            //lead.setContactTypeId(java.lang.Long contactTypeId)

            System.out.println("TESTS - fetching entities to update with ManyToOne entities and persisting changes");

            System.out.println("TESTS - fetching entities to remove oneToMany relationships and persisting changes");

            //UPDATE "lead" SET modify_date = '2023-04-06 10:32:47.599',product_id = 3,civil_reg_no = '05055555131342',first_name = 'test_firstname',email_address = 'testing@gmail.com',phone_number = '0123456789',status_id = null,processing_id = '214a1839-a18b-483f-9859-91c04ed355811',surname = 'test_surname',attached_pdf_document_data = null WHERE "contact_id" = 51

            System.out.println("TESTS - fetching entities to remove ManyToOne relationships and persisting changes");
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("TESTS - CLEAN UP DATA");
            if(statusTypeRepository != null) {
                leadRepository.deleteAllNoException();
            }
            if(statusTypeRepository != null) {
                statusTypeRepository.deleteAllNoException();
            }
        }

        System.out.println("TESTS - END");
    }

    public static Calendar nowCal() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(UTC));
        return calendar;
    }

    public static Date now() {
        return nowCal().getTime();
    }

    public static java.sql.Timestamp nowDbTimestamp() {
        return new java.sql.Timestamp(nowCal().getTimeInMillis());
    }

}
