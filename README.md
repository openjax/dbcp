<img src="https://images.cooltext.com/5195724.png" align="right">

## dbcp<br><a href="https://www.easyjax.org/"><img src="https://img.shields.io/badge/EasyJAX--blue.svg"></a>
> Database Connection Pool

### Introduction

**dbcp** is a light wrapper around the [Apache Commons DBCP][apache-commons-dbcp] library, which provides a simple API to describe and initialize a JDBC Database Connection Pool.

### Why **dbcp**?

#### CohesionFirst

Developed with the CohesionFirst approach, **dbcp** is an easy-to-use and simple solution that separates itself from the rest with the strength of its cohesion and ease of usability. Made possible by the rigorous conformance to best practices in every line of its implementation, **dbcp** considers the needs of the developer as primary, and offers a complete solution for the command line arguments facet of an application.

#### Complete Solution

**dbcp** allows a developer to configure a Connection Pool with a [standardized XML Schema][dbcp-schema], which is used by a consumer class to initiate the connection pool. **dbcp** uses the JAXB framework to significantly reduce the boilerplate code, thus providing a lean API with support for the all possible connection pool configuration variations.

#### Validating and Fail-Fast

**dbcp** is based on a [XML Schema][dbcp-schema] used to specify the formal of XML documents accepted by the configuration consumer. The XML Schema is designed to use the full power of XML Validation to allow a developer to qiuckly determine errors in his draft. Once a `dbcp.xml` passes the validation checks, it is almost guaranteed to properly initialize the Connection Pool configured by the file.

### Getting Started

#### Prerequisites

* [Java 8][jdk8-download] - The minimum required JDK version.
* [Maven][maven] - The dependency management system.

#### Example (Quick-&-Easy)

1. In your preferred development directory, create a [`dbcp-maven-archetype`][dbcp-maven-archetype] project.

    ```tcsh
    mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app \
    -DarchetypeGroupId=org.openjax.maven.archetype -DarchetypeArtifactId=dbcp-maven-archetype \
    -DarchetypeCatalog=http://mvn.repo.openjax.org -DinteractiveMode=false
    ```

#### Example (Hands-on)

1. In your preferred development directory, create a [`maven-archetype-quickstart`][maven-archetype-quickstart] project.

    ```tcsh
    mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
    ```

2. Add the `mvn.repo.openjax.org` Maven repositories to the POM.

    ```xml
    <repositories>
      <repository>
        <id>mvn.repo.openjax.org</id>
        <url>http://mvn.repo.openjax.org/m2</url>
      </repository>
    </repositories>
    <pluginRepositories>
      <pluginRepository>
        <id>mvn.repo.openjax.org</id>
        <url>http://mvn.repo.openjax.org/m2</url>
      </pluginRepository>
    </pluginRepositories>
    ```

3. Create a `dbcp.xml` in `src/main/resources/`.

    ```xml
    <dbcp
      xmlns="http://easyjax.org/dbcp-1.0.4.xsd"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://easyjax.org/dbcp-1.0.4.xsd http://easyjax.org/dbcp.xsd"
      name="basis">
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

4. Add `org.easyjax:dbcp` dependency to the POM.

    ```xml
    <dependency>
      <groupId>org.easyjax</groupId>
      <artifactId>dbcp</artifactId>
      <version>1.0.4-SNAPSHOT</version>
    </dependency>
    ```

5. In the `main()` method in `App.java`, add the following line and let your IDE resolve the missing imports.

    ```java
    DataSource dataSource = DataSources.createDataSource(Thread.currentThread().getContextClassLoader().getResource("dbcp.xml"));
    ```

    The `dataSource` object is a reference to the initialized JDBC Connection Pool configured in `dbcp.xml`.

### License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

<a href="http://cooltext.com" target="_top"><img src="https://cooltext.com/images/ct_pixel.gif" width="80" height="15" alt="Cool Text: Logo and Graphics Generator" border="0" /></a>

[apache-commons-dbcp]: https://commons.apache.org/proper/commons-dbcp
[dbcp-maven-archetype]: /../../../../easyjax/dbcp-maven-archetype
[dbcp-schema]: /../../../../easyjax/dbcp/blob/master/src/main/resources/dbcp.xsd
[jdk8-download]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[maven-archetype-quickstart]: http://maven.apache.org/archetypes/maven-archetype-quickstart/
[maven]: https://maven.apache.org/