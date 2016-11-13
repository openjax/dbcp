<img src="http://safris.org/logo.png" align="right" />
# DBCP [![CohesionFirst](http://safris.org/cf2.svg)](https://cohesionfirst.com/)
> Database Connection Pool

## Introduction

DBCP is a lightweight wrapper around the [Apache Commons DBCP](https://commons.apache.org/proper/commons-dbcp/) library that provides a simple way to describe and initialize a JDBC Database Connection Pool.

## Why XDL?

### CohesionFirst™

Developed with the CohesionFirst™ approach, DBCP is the cohesive alternative to the description and initialization of Database Connection Pools in JDBC. Made possible by the rigorous conformance to design patterns and best practices in every line of its implementation, DBCP is a complete solution for the JDBC Connection Pool specification.

### Complete Solution

DBCP allows a developer to configure a Connection Pool with a [standardized XML Schema](https://cf.safris.org/xdl.xsd), which is used by a consumer class to initiate the pool. DBCP uses the XSB framework for XML Schema Binding to significantly reduce the boilerplate code, thus providing a lean API with support for the full space of DBCP configuration variations.

### Validating and Fail-Fast

DBCP is based on a XML Schema used to specify the formal of XML documents accepted by the configuration consumer. The XML Schema is designed to use the full power of XML Validation to allow a developer to qiuckly determine errors in his draft. Once a `dbcp.xml` passes the validation checks, it is almost guaranteed to properly initialize the Connection Pool configured by the file.

## Getting Started

### Prerequisites

* [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) - The minimum required JDK version.
* [Maven](https://maven.apache.org/) - The dependency management system used to install XDL.

### Example

1. In your preferred development directory, create a [`maven-archetype-quickstart`](http://maven.apache.org/archetypes/maven-archetype-quickstart/) project.

  ```tcsh
  mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
  ```

2. Add the `mvn.repo.safris.org` Maven repositories to the POM.

  ```xml
  <repositories>
    <repository>
      <id>mvn.repo.safris.org</id>
      <url>http://mvn.repo.safris.org/m2</url>
    </repository>
  </repositories>
  <pluginRepositories>
    <pluginRepository>
      <id>mvn.repo.safris.org</id>
      <url>http://mvn.repo.safris.org/m2</url>
    </pluginRepository>
  </pluginRepositories>
  ```

3. Create a `dbcp.xml` in `src/main/resources/`.

  ```xml
  <dbcp name="basis"
    xmlns="http://commons.safris.org/dbcp.xsd"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://commons.safris.org/dbcp.xsd http://commons.safris.org/dbcp.xsd">
    <jdbc>
      <url>jdbc:postgresql://localhost/basis</url>
      <driverClassName>org.postgresql.Driver</driverClassName>
      <username>basis</username>
      <password>basis</password>
      <loginTimeout>5000</loginTimeout>
    </jdbc>
    <default>
      <autoCommit>true</autoCommit>
      <readOnly>false</readOnly>
      <transactionIsolation>READ_UNCOMMITTED</transactionIsolation>
    </default>
    <size>
      <initialSize>0</initialSize>
      <maxActive>16</maxActive>
      <maxIdle>16</maxIdle>
      <minIdle>0</minIdle>
      <maxWait>1000</maxWait>
    </size>
    <management>
      <timeBetweenEvictionRuns>-1</timeBetweenEvictionRuns>
      <numTestsPerEvictionRun>3</numTestsPerEvictionRun>
      <minEvictableIdleTime>1800000</minEvictableIdleTime>
    </management>
    <preparedStatements>
      <poolPreparedStatements>false</poolPreparedStatements>
      <maxOpenPreparedStatements>-1</maxOpenPreparedStatements>
    </preparedStatements>
    <removal>
      <removeAbandoned>false</removeAbandoned>
      <removeAbandonedTimeout>300</removeAbandonedTimeout>
      <logAbandoned>false</logAbandoned>
    </removal>
    <logging>
      <level>ALL</level>
      <logExpiredConnections>true</logExpiredConnections>
      <logAbandoned>true</logAbandoned>
    </logging>
  </dbcp>
  ```

4. Add `org.safris.commons`:`dbcp` dependency to the POM.

  ```xml
  <dependency>
    <groupId>org.safris.commons</groupId>
    <artifactId>dbcp</artifactId>
    <version>2.0.2</version>
  </dependency>
  ```

5. In the `main()` method in `App.java`, add the following line and let your IDE resolve the missing imports.

  ```java
  final dbcp_dbcp dbcp = (dbcp_dbcp)Bindings.parse(new InputSource(Resources.getResourceOrFile("dbcp.xml").getURL().openStream()));
  final DataSource dataSource = DataSources.createDataSource(dbcp);
  ```

  The `dataSource` object is a reference to the initialized JDBC Connection Pool configured in `dbcp.xml`.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.
