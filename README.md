# LightWeight_Java_PSQL_Framework
Lightweight Java PSQL Database interaction framework built to be used effectively on lambdas running on Java but can be used for traditional apps as well

For Java 11+

Dependency size is small (what lambdas need when cold booting), I tried to keep it as small as possible, but further improvements could perhaps still be made

It uses Apache's DB Utils as its core, with more code wrapped around that but it still allows you to go full base and just execute raw queries

This is for people sick of the over-bloatedesness and over-abstraction of Hibernate or JPA/JTA frameworks, this is not a full replacement for those frameworks, but a "light" replacement, it could become a good "light" replacement in the future...

### IMPORTANT - Do not use primitive values for fields, for instance use Integer not int or Long not long
### do not pass in more than 30000 rows/entities at a time when calling methods (unless its for delete or a get), I am not sure if the ApachaUtils core framework code has setup logic to handle this, do at your own risk or split up the calls, I will test this soon and fix it if I have to, 34000+- rows migth be a limit for the IN operator in PSQL

#### set the following properties inside you properties file: 
- datasource.url or DATABASE_URL(must contain full connection path plus db name)
- datasource.username or DATABASE_USERNAME
- datasource.password or DATABASE_PASSWORD

#### optionally set the following properties inside your properties file:
- datasource.max_db_connection_pool_size or DB_CONNECTION_POOL_SIZE (defaults to 40)

### example code (with tests in TestMain Class):(https://github.com/ricomaster9000/LightWeight_Java_PSQL_Framework/tree/main/testing/src/main/java/org/greatgamesonly/shared/opensource/sql/framework/lightweightsql/testing)

Also, every public method in the BaseRepository class is usable for every defined Repository class, these come available from the start, there are many methods (in  https://github.com/ricomaster9000/LightWeight_Java_PSQL_Framework/blob/main/src/main/java/org/greatgamesonly/shared/opensource/sql/framework/lightweightsql/database/BaseRepository.java)


add as dependency by using jitpack.io, go to this link: https://jitpack.io/#ricomaster9000/LightWeight_Java_PSQL_Framework/1.4.2.1

Will upload to Maven later, once I am fully done and have more time
