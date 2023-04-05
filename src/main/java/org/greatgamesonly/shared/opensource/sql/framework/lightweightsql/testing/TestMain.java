package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.testing;

public class TestMain {

    public static void main(String[] args) {
        doTests();
        System.exit(0);
    }

    private static void doTests() {
        System.out.println("TESTS - BEGIN");

        try {
            System.out.println("TESTS - Setting up repositories&connection-pools and establishing connection");

            System.out.println("TESTS - Creating new data and inserting that data");

            System.out.println("TESTS - Fetching newly created data and updating that data and persisting changes");

            System.out.println("TESTS - Fetching updated data");

            System.out.println("TESTS - Fetching updated data in a filtered way");

            System.out.println("TESTS - Fetching updated data and adding oneToMany relationships then persist changing");

            System.out.println("TESTS - Fetching updated data with oneToMany relationships");

            System.out.println("TESTS - Creating new data for ManyToOneRelationships and inserting data");

            System.out.println("TESTS - Creating manyToOne data");

            System.out.println("TESTS - fetching data to update with ManyToOne and persisting changes");

            System.out.println("TESTS - fetching data to remove oneToMany relationships");

            System.out.println("TESTS - fetching data to remove ManyToOne relationships");

            System.out.println("TESTS - remove all remaining data");
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("TESTS - END");
    }

}
