
make sure you are in the base project directory, run below to run tests

mvn -f testing compile exec:java -Dexec.mainClass="org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.testing.TestMain" -Dexec.args="${{ secrets.DATABASE_URL }} ${{ secrets.DATABASE_USERNAME }} ${{ secrets.DATABASE_PASSWORD }}
