# LightWeight_Java_PSQL_Framework
Lightweight Java PSQL Database interaction framework built to be used effectively on lambdas running on Java but can be used for traditional apps as well

For Java 11+

Dependency size is small (what lambdas need when cold booting), I tried to keep it as small as possible, but further improvements could perhaps still be made

It uses Apache's DB Utils as its core, with more code wrapped around that but it still allows you to go full base and just execute raw queries

This is for people sick of the over-bloatedesness and over-abstraction of Hibernate or JPA/JTA frameworks, this is not a full replacement for those frameworks, but a "light" replacement, it could become a good "light" replacement in the future...

See example package for some examples on how to use this library/small-framework
(https://github.com/ricomaster9000/LightWeight_Java_PSQL_SQL_Framework/tree/main/src/main/java/org/greatgamesonly/shared/opensource/sql/framework/lightweightsql/example)

add as dependency by using jitpack.io, go to this link: https://jitpack.io/#ricomaster9000/LightWeight_Java_PSQL_Framework/1.1.0

Will upload to Maven later, once I am fully done and have more time
