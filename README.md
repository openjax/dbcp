# DBCP

[![Build Status](https://travis-ci.org/openjax/dbcp.svg?1)](https://travis-ci.org/openjax/dbcp)
[![Coverage Status](https://coveralls.io/repos/github/openjax/dbcp/badge.svg?1)](https://coveralls.io/github/openjax/dbcp)
[![Javadocs](https://www.javadoc.io/badge/org.openjax/dbcp.svg?1)](https://www.javadoc.io/doc/org.openjax/dbcp)
[![Released Version](https://img.shields.io/maven-central/v/org.openjax/dbcp.svg?1)](https://mvnrepository.com/artifact/org.openjax/dbcp)
![Snapshot Version](https://img.shields.io/nexus/s/org.openjax/dbcp?label=maven-snapshot&server=https%3A%2F%2Foss.sonatype.org)

## Introduction

OpenJAX DBCP is a light wrapper around the [Apache Commons DBCP][apache-commons-dbcp] library, which provides a simple API to describe and initialize a JDBC Database Connection Pool.

OpenJAX DBCP allows a developer to configure a Connection Pool with a [standardized XML Schema][dbcp-schema], which is used by a consumer class to initiate the connection pool. **dbcp** uses the JAXB framework to significantly reduce the boilerplate code, thus providing a lean API with support for the all possible connection pool configuration variations.

### Validating and Fail-Fast

OpenJAX DBCP is based on a [XML Schema][dbcp-schema] used to specify the formal of XML documents accepted by the configuration consumer. The XML Schema is designed to use the full power of XML Validation to allow a developer to qiuckly determine errors in his draft. Once a `dbcp.xml` passes the validation checks, it is almost guaranteed to properly initialize the Connection Pool configured by the file.

## Getting Started

### Prerequisites

* [Java 8][jdk8-download] - The minimum required JDK version.
* [Maven][maven] - The dependency management system.

### Example

1. Create a `dbcp.xml` in `src/main/resources/`.

   ```xml
   <dbcp name="example"
     xmlns="http://www.openjax.org/dbcp-1.1.xsd"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.openjax.org/dbcp-1.1.xsd http://www.openjax.org/dbcp.xsd">
     <jdbc>
       <url>jdbc:derby:memory:example;create=true</url>
       <driverClassName>org.apache.derby.jdbc.EmbeddedDriver</driverClassName>
     </jdbc>
     <default>
       <catalog>catalog</catalog>
       <autoCommit>true</autoCommit>
       <readOnly>false</readOnly>
       <queryTimeout>300000</queryTimeout>
       <transactionIsolation>READ_UNCOMMITTED</transactionIsolation>
     </default>
     <connection>
       <properties>
         <property name="prop1" value="value1"/>
         <property name="prop2" value="value2"/>
       </properties>
       <initSqls>
         <initSql>SELECT 1 FROM SYSIBM.SYSDUMMY1</initSql>
         <initSql>SELECT 1 FROM SYSIBM.SYSDUMMY1</initSql>
       </initSqls>
     </connection>
     <size>
       <initialSize>0</initialSize>
       <maxTotal>8</maxTotal>
       <maxIdle>8</maxIdle>
       <minIdle>0</minIdle>
       <maxOpenPreparedStatements>INDEFINITE</maxOpenPreparedStatements>
     </size>
     <pool>
       <queue>lifo</queue>
       <cacheState>false</cacheState>
       <maxWait>INDEFINITE</maxWait>
       <maxConnectionLifetime>INDEFINITE</maxConnectionLifetime>
       <autoCommitOnReturn>true</autoCommitOnReturn>
       <rollbackOnReturn>true</rollbackOnReturn>
       <removeAbandoned on="maintenance" timeout="300"/>
       <abandonedUsageTracking>true</abandonedUsageTracking>
       <allowAccessToUnderlyingConnection>false</allowAccessToUnderlyingConnection>
       <eviction>
         <timeBetweenRuns>300000</timeBetweenRuns>
         <numTestsPerRun>3</numTestsPerRun>
         <minIdleTime>1800000</minIdleTime>
         <softMinIdleTime>INDEFINITE</softMinIdleTime>
         <policyClassName>org.openjax.dbcp.MockEvictionPolicy</policyClassName>
       </eviction>
     </pool>
     <validation>
       <query>SELECT 1 FROM SYSIBM.SYSDUMMY1</query>
       <testOnBorrow>false</testOnBorrow>
       <testOnReturn>false</testOnReturn>
       <testWhileIdle>false</testWhileIdle>
       <fastFail>
         <disconnectionSqlCodes>42X01 42X02 42X03</disconnectionSqlCodes>
       </fastFail>
     </validation>
     <logging>
       <level>INFO</level>
       <logExpiredConnections>true</logExpiredConnections>
       <logAbandoned>true</logAbandoned>
     </logging>
   </dbcp>
   ```

1. Add `org.openjax:dbcp` dependency to the POM.

   ```xml
   <dependency>
     <groupId>org.openjax</groupId>
     <artifactId>dbcp</artifactId>
     <version>1.1.1</version>
   </dependency>
   ```

1. In the `main()` method in `App.java`, add the following line and let your IDE resolve the missing imports.

   ```java
   DataSource dataSource = DataSources.createDataSource(ClassLoader.getSystemClassLoader().getResource("dbcp.xml"));
   ```

    The `dataSource` object is a reference to the initialized JDBC Connection Pool configured in `dbcp.xml`.

## Contributing

Pull requests are welcome. For major changes, please [open an issue](../../issues) first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

[apache-commons-dbcp]: https://commons.apache.org/proper/commons-dbcp
[dbcp-schema]: /src/main/resources/dbcp.xsd
[jdk8-download]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[maven-archetype-quickstart]: http://maven.apache.org/archetypes/maven-archetype-quickstart/
[maven]: https://maven.apache.org/