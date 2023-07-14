
make sure you are in the base project directory, run below to run tests

mvn -f testing clean install test -DDATABASE_URL=${{ secrets.DATABASE_URL }} -DDATABASE_USERNAME=${{ secrets.DATABASE_USERNAME }} -DDATABASE_PASSWORD=${{ secrets.DATABASE_PASSWORD }}
