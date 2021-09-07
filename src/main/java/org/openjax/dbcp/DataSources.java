/* Copyright (c) 2008 OpenJAX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.openjax.dbcp;

import static org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.libj.lang.Assertions;
import org.libj.logging.LoggerPrintWriter;
import org.openjax.dbcp_1_2.Dbcp;
import org.openjax.www.dbcp_1_2.xL0gluGCXAA.$Dbcp;
import org.openjax.www.dbcp_1_2.xL0gluGCXAA.Dbcps;
import org.openjax.www.xml.datatypes_0_9.xL9gluGCXAA.$StringNonEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.xml.sax.SAXException;

public final class DataSources {
  private static final List<String> defaultDisconnectionQueryCodes = Arrays.asList("57P01", "57P02", "57P03", "01002", "JZ0C0", "JZ0C1");
  private static final String INDEFINITE = "INDEFINITE";
  private static final String schemaFile = "dbcp.xsd";
  private static Schema schema;

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * {@link URL url} specifying an xml document with root element
   * {@code dbcp:dbcp}. {@link ClassLoader#getSystemClassLoader()} will be used
   * by the {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param url An {@link URL} specifying a dbcp xml resource.
   * @return The {@link BasicDataSource} instance.
   * @throws IOException If an I/O error has occurred
   * @throws SAXException If the xml document does not have a {@code dbcp:dbcp}
   *           root element, or if an XML validation error has occurred.
   * @throws IllegalArgumentException If {@code url} is null, or if the
   *           {@code /dbcp:dbcp/dbcp:jdbc} element is missing.
   */
  public static BasicDataSource createDataSource(final URL url) throws IOException, SAXException {
    return createDataSource(url, ClassLoader.getSystemClassLoader());
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * {@link URL url} specifying an xml document with root element
   * {@code dbcp:dbcp}. {@link ClassLoader#getSystemClassLoader()} will be used
   * by the {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param url An {@link URL} specifying a dbcp xml resource.
   * @param driverClassLoader Class loader to be used by the
   *          {@link BasicDataSource} when it loads the JDBC driver.
   * @return The {@link BasicDataSource} instance.
   * @throws IOException If an I/O error has occurred
   * @throws SAXException If the xml document does not have a {@code dbcp:dbcp}
   *           root element, or if an XML validation error has occurred.
   * @throws IllegalArgumentException If {@code url} is null, or if the
   *           {@code /dbcp:dbcp/dbcp:jdbc} element is missing.
   */
  public static BasicDataSource createDataSource(final URL url, final ClassLoader driverClassLoader) throws IOException, SAXException {
    try {
      final Unmarshaller unmarshaller = JAXBContext.newInstance(Dbcp.class).createUnmarshaller();
      final URL resource = Thread.currentThread().getContextClassLoader().getResource(schemaFile);
      if (resource == null)
        throw new IllegalStateException("Unable to find " + schemaFile + " in class loader " + Thread.currentThread().getContextClassLoader());

      unmarshaller.setSchema(DataSources.schema == null ? DataSources.schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(resource) : DataSources.schema);
      try (final InputStream in = Assertions.assertNotNull(url).openStream()) {
        final JAXBElement<Dbcp> element = unmarshaller.unmarshal(XMLInputFactory.newInstance().createXMLStreamReader(in), Dbcp.class);
        return createDataSource(driverClassLoader, element.getValue());
      }
    }
    catch (final FactoryConfigurationError e) {
      throw new UnsupportedOperationException(e);
    }
    catch (final JAXBException | XMLStreamException e) {
      throw new SAXException(e);
    }
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * array of {@link Dbcp dbcp} JAX-B bindings that match the specified
   * {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the
   * {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param id The id of the {@link Dbcp dbcp} bindings to match, or
   *          {@code null} to match all bindings.
   * @param dbcps Array of {@link Dbcp} JAX-B bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws IllegalArgumentException If {@code dbcps} is null, any member of
   *           {@code dbcps} is null, or if the {@code /dbcp:jdbc} element is
   *           missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final Dbcp ... dbcps) {
    return createDataSource(id, ClassLoader.getSystemClassLoader(), dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * array of {@link $Dbcp dbcp} JAX-SB bindings that match the specified
   * {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the
   * {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param id The id of the {@code /dbcp:dbcp} child elements of the provided
   *          {@link $Dbcp} to match, or {@code null} to match all child
   *          elements.
   * @param dbcps Array of {@link $Dbcp} JAX-SB bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws IllegalArgumentException If {@code dbcps} is null, any member of
   *           {@code dbcps} is null, or if the {@code /dbcp:jdbc} element is
   *           missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final $Dbcp ... dbcps) {
    return createDataSource(id, ClassLoader.getSystemClassLoader(), dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * {@code /dbcp:dbcp} child elements of the provided
   * {@link org.openjax.dbcp_1_2.Dbcps} JAX-B binding that match the specified
   * {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the
   * {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param id The id of the {@code /dbcp:dbcp} child elements of the provided
   *          {@link org.openjax.dbcp_1_2.Dbcps} to match, or {@code null} to
   *          match all child elements.
   * @param dbcps The {@link org.openjax.dbcp_1_2.Dbcps} JAX-B binding.
   * @return The {@link BasicDataSource} instance.
   * @throws IllegalArgumentException If {@code dbcps} is null, does not contain
   *           any {@code /dbcp:dbcp} child elements, contains null child
   *           elements, or if the {@code /dbcp:jdbc} element is missing from
   *           all {@code /dbcp:dbcp} child elements in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final org.openjax.dbcp_1_2.Dbcps dbcps) {
    return createDataSource(id, ClassLoader.getSystemClassLoader(), dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * {@code /dbcp:dbcp} child elements of the provided {@link Dbcps} JAX-SB
   * binding that match the specified {@code id}.
   * {@link ClassLoader#getSystemClassLoader()} will be used by the
   * {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param id The id of the {@code /dbcp:dbcp} child elements of the provided
   *          {@link Dbcps} to match, or {@code null} to match all child
   *          elements.
   * @param dbcps The {@link Dbcps} JAX-SB binding.
   * @return The {@link BasicDataSource} instance.
   * @throws IllegalArgumentException If {@code dbcps} is null, does not contain
   *           any {@code /dbcp:dbcp} child elements, contains null child
   *           elements, or if the {@code /dbcp:jdbc} element is missing from
   *           all {@code /dbcp:dbcp} child elements in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final Dbcps dbcps) {
    return createDataSource(id, ClassLoader.getSystemClassLoader(), dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * {@code /dbcp:dbcp} child elements of the provided {@link Dbcps} JAX-B
   * binding that match any {@code id}.
   *
   * @param driverClassLoader Class loader to be used by the
   *          {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps Array of {@link Dbcp} JAX-B bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws IllegalArgumentException If {@code dbcps} is null, any member of
   *           {@code dbcps} is null, or if the {@code /dbcp:jdbc} element is
   *           missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final ClassLoader driverClassLoader, final Dbcp ... dbcps) {
    return createDataSource(null, driverClassLoader, Assertions.assertNotNull(dbcps));
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * array of {@link $Dbcp dbcp} JAX-SB bindings that match the specified
   * {@code id}.
   *
   * @param driverClassLoader Class loader to be used by the
   *          {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps Array of {@link $Dbcp} JAX-SB bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws IllegalArgumentException If {@code dbcps} is null, any member of
   *           {@code dbcps} is null, or if the {@code /dbcp:jdbc} element is
   *           missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final ClassLoader driverClassLoader, final $Dbcp ... dbcps) {
    return createDataSource(null, driverClassLoader, Assertions.assertNotNull(dbcps));
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * {@code /dbcp:dbcp} child elements of the provided
   * {@link org.openjax.dbcp_1_2.Dbcps} JAX-B binding that match the specified
   * {@code id}.
   *
   * @param id The id of the {@code /dbcp:dbcp} child elements of the provided
   *          {@link org.openjax.dbcp_1_2.Dbcps} to match, or {@code null} to
   *          match all child elements.
   * @param driverClassLoader Class loader to be used by the
   *          {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps The {@link org.openjax.dbcp_1_2.Dbcps} JAX-B binding.
   * @return The {@link BasicDataSource} instance.
   * @throws IllegalArgumentException If {@code dbcps} is null, does not contain
   *           any {@code /dbcp:dbcp} child elements, contains null child
   *           elements, or if the {@code /dbcp:jdbc} element is missing from
   *           all {@code /dbcp:dbcp} child elements in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final ClassLoader driverClassLoader, final org.openjax.dbcp_1_2.Dbcps dbcps) {
    Assertions.assertNotNull(dbcps);
    Assertions.assertNotNull(dbcps.getDbcp());
    return createDataSource(id, driverClassLoader, dbcps.getDbcp().toArray(new $Dbcp[dbcps.getDbcp().size()]));
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * {@code /dbcp:dbcp} child elements of the provided {@link Dbcps} JAX-SB
   * binding that match the specified {@code id}.
   *
   * @param id The id of the {@code /dbcp:dbcp} child elements of the provided
   *          {@link Dbcps} to match, or {@code null} to match all child
   *          elements.
   * @param driverClassLoader Class loader to be used by the
   *          {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps The {@link Dbcps} JAX-SB binding.
   * @return The {@link BasicDataSource} instance.
   * @throws IllegalArgumentException If {@code dbcps} is null, does not contain
   *           any {@code /dbcp:dbcp} child elements, contains null child
   *           elements, or if the {@code /dbcp:jdbc} element is missing from
   *           all {@code /dbcp:dbcp} child elements in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final ClassLoader driverClassLoader, final Dbcps dbcps) {
    Assertions.assertNotNull(dbcps);
    Assertions.assertNotNull(dbcps.getDbcpDbcp());
    return createDataSource(id, driverClassLoader, dbcps.getDbcpDbcp().toArray(new $Dbcp[dbcps.getDbcpDbcp().size()]));
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * array of {@link Dbcp dbcp} JAX-B bindings that match the specified
   * {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the
   * {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param dbcp The {@link Dbcp} JAX-B bindings providing the configuration.
   * @return The {@link BasicDataSource} instance.
   * @throws IllegalArgumentException If {@code dbcp} is null or if the
   *           {@code /dbcp:jdbc} element is missing.
   */
  public static BasicDataSource createDataSource(final Dbcp dbcp) {
    return createDataSource(null, ClassLoader.getSystemClassLoader(), dbcp);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * array of {@link $Dbcp dbcp} JAX-SB bindings that match the specified
   * {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the
   * {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param dbcp The {@link $Dbcp} JAX-SB bindings providing the configuration.
   * @return The {@link BasicDataSource} instance.
   * @throws IllegalArgumentException If {@code dbcp} is null or if the
   *           {@code /dbcp:jdbc} element is missing.
   */
  public static BasicDataSource createDataSource(final $Dbcp dbcp) {
    return createDataSource(null, ClassLoader.getSystemClassLoader(), dbcp);
  }

  private final ClassLoader driverClassLoader;
  private BasicDataSource dataSource = null;
  private String driverClassName = null;
  private String url = null;

  private boolean autoCommit = true;
  private boolean readOnly = false;

  private Integer queryTimeout = null;
  private String transactionIsolation = null;

  private int initialSize = 0;
  private int minIdle = 0;
  private String maxIdle = INDEFINITE;
  private String maxTotal = INDEFINITE;
  private boolean poolPreparedStatements = false;
  private String maxOpen = INDEFINITE;

  private boolean lifo = true;

  private boolean cacheState = true;
  private String maxWait = INDEFINITE;
  private String maxConnLifetime = INDEFINITE;
  private boolean autoCommitOnReturn = true;
  private boolean rollbackOnReturn = true;
  private String removeAbandonedOn = null;
  private int removeAbandonedTimeout = 0;
  private boolean abandonedUsageTracking = false;
  private boolean accessToUnderlyingConnectionAllowed = false;
  private boolean hasEviction = false;
  private String timeBetweenEvictionRunsMillis = INDEFINITE;
  private int numTestsPerRun = 3;
  private long minEvictableIdleTimeMillis = 1800000;
  private String softMinEvictableIdleTimeMillis = INDEFINITE;
  private String policyClassName = null;

  private boolean hasValidation = false;
  private String validationQuery = null;
  private String validationQueryTimeout = INDEFINITE;
  private boolean testOnCreate = false;
  private boolean testOnBorrow = true;
  private boolean testOnReturn = false;
  private boolean testWhileIdle = false;
  private List<String> disconnectionQueryCodes = null;

  private String loggingLevel = null;
  private boolean logExpiredConnections = false;
  private boolean logAbandoned = false;

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * array of {@link Dbcp dbcp} JAX-B bindings that match the specified
   * {@code id}.
   *
   * @param id The id of the {@link Dbcp dbcp} bindings to match, or
   *          {@code null} to match all bindings.
   * @param driverClassLoader Class loader to be used by the
   *          {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps Array of {@link Dbcp} JAX-B bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws IllegalArgumentException If {@code dbcps} is null, any member of
   *           {@code dbcps} is null, or if the {@code /dbcp:jdbc} element is
   *           missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final ClassLoader driverClassLoader, final Dbcp ... dbcps) {
    Assertions.assertNotNull(dbcps);
    return new DataSources(id, driverClassLoader, dbcps).build();
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the
   * array of {@link $Dbcp dbcp} JAX-SB bindings that match the specified
   * {@code id}.
   *
   * @param id The id of the {@link Dbcp dbcp} bindings to match, or
   *          {@code null} to match all bindings.
   * @param driverClassLoader Class loader to be used by the
   *          {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps Array of {@link $Dbcp} JAX-SB bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws IllegalArgumentException If {@code dbcps} is null, any member of
   *           {@code dbcps} is null, or if the {@code /dbcp:jdbc} element is
   *           missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final ClassLoader driverClassLoader, final $Dbcp ... dbcps) {
    Assertions.assertNotNull(dbcps);
    return new DataSources(id, driverClassLoader, dbcps).build();
  }

  private DataSources(final String id, final ClassLoader driverClassLoader, final Dbcp ... dbcps) {
    this.driverClassLoader = driverClassLoader;
    for (final Dbcp dbcp : dbcps) {
      Assertions.assertNotNull(dbcp);
      if (id != null && !id.equals(dbcp.getId()))
        continue;

      if (dataSource == null)
        dataSource = new BasicDataSource();

      if (dbcp.getJdbc() != null) {
        if (dbcp.getJdbc().getDriverClassName() != null)
          driverClassName = dbcp.getJdbc().getDriverClassName();

        if (dbcp.getJdbc().getUrl() != null)
          url = dbcp.getJdbc().getUrl();
      }

      final Dbcp.Default _default = dbcp.getDefault();
      if (_default != null) {
        if (_default.getCatalog() != null)
          dataSource.setDefaultCatalog(_default.getCatalog());

        if (_default.getAutoCommit() != null)
          autoCommit = _default.getAutoCommit();

        if (_default.getReadOnly() != null)
          readOnly = _default.getReadOnly();

        if (_default.getQueryTimeout() != null)
          queryTimeout = _default.getQueryTimeout();

        if (_default.getTransactionIsolation() != null)
          transactionIsolation = _default.getTransactionIsolation();
      }

      final Dbcp.Connection connection = dbcp.getConnection();
      if (connection != null) {
        if (connection.getProperties() != null)
          for (final Dbcp.Connection.Properties.Property property : connection.getProperties().getProperty())
            if (property.getName() != null && property.getValue() != null)
              dataSource.addConnectionProperty(property.getName(), property.getValue());

        if (connection.getInitSqls() != null) {
          if (dataSource.getConnectionInitSqls().size() > 0)
            dataSource.getConnectionInitSqls().addAll(connection.getInitSqls().getInitSql());
          else
            dataSource.setConnectionInitSqls(connection.getInitSqls().getInitSql());
        }
      }

      final Dbcp.Size size = dbcp.getSize();
      if (size != null) {
        if (size.getInitialSize() != null)
          initialSize = size.getInitialSize();

        if (size.getMinIdle() != null)
          minIdle = size.getMinIdle();

        if (size.getMaxIdle() != null)
          maxIdle = size.getMaxIdle();

        if (size.getMaxTotal() != null)
          maxTotal = size.getMaxTotal();

        if (size.getPoolPreparedStatements() != null) {
          poolPreparedStatements = true;
          if (size.getPoolPreparedStatements().getMaxOpen() != null)
            maxOpen = size.getPoolPreparedStatements().getMaxOpen();
        }
      }

      final Dbcp.Pool pool = dbcp.getPool();
      if (pool != null) {
        if (pool.getQueue() != null) {
          if ("lifo".equals(pool.getQueue()))
            lifo = true;
          else if ("fifo".equals(pool.getQueue()))
            lifo = false;
          else
            throw new UnsupportedOperationException("Unsupported queue spec: " + pool.getQueue());
        }

        if (pool.getCacheState() != null)
          cacheState = pool.getCacheState();

        if (pool.getMaxWait() != null)
          maxWait = pool.getMaxWait();

        if (pool.getMaxConnectionLifetime() != null)
          maxConnLifetime = pool.getMaxConnectionLifetime();

        if (pool.getAutoCommitOnReturn() != null)
          autoCommitOnReturn = pool.getAutoCommitOnReturn();

        if (pool.getRollbackOnReturn() != null)
          rollbackOnReturn = pool.getRollbackOnReturn();

        if (pool.getRemoveAbandoned() != null) {
          removeAbandonedOn = pool.getRemoveAbandoned().getOn();
          removeAbandonedTimeout = pool.getRemoveAbandoned().getTimeout();
        }

        if (pool.getAbandonedUsageTracking() != null)
          abandonedUsageTracking = pool.getAbandonedUsageTracking();

        if (pool.getAllowAccessToUnderlyingConnection() != null)
          accessToUnderlyingConnectionAllowed = pool.getAllowAccessToUnderlyingConnection();


        final Dbcp.Pool.Eviction eviction = pool.getEviction();
        if (eviction != null) {
          hasEviction = true;
          if (eviction.getTimeBetweenRuns() != null)
            timeBetweenEvictionRunsMillis = eviction.getTimeBetweenRuns();

          if (eviction.getNumTestsPerRun() != null)
            numTestsPerRun = eviction.getNumTestsPerRun();

          if (eviction.getMinIdleTime() != null)
            minEvictableIdleTimeMillis = eviction.getMinIdleTime();

          if (eviction.getSoftMinIdleTime() != null)
            softMinEvictableIdleTimeMillis = eviction.getSoftMinIdleTime();

          if (eviction.getPolicyClassName() != null)
            policyClassName = eviction.getPolicyClassName();
        }
      }

      final Dbcp.Validation validation = dbcp.getValidation();
      if (validation != null) {
        hasValidation = true;
        if (validation.getQuery() != null)
          validationQuery = validation.getQuery();

        if (validation.getTimeout() != null)
          validationQueryTimeout = validation.getTimeout();

        if (validation.getTestOnCreate() != null)
          testOnCreate = validation.getTestOnCreate();

        if (validation.getTestOnBorrow() != null)
          testOnBorrow = validation.getTestOnBorrow();

        if (validation.getTestOnReturn() != null)
          testOnReturn = validation.getTestOnReturn();

        if (validation.getTestWhileIdle() != null)
          testWhileIdle = validation.getTestWhileIdle();

        final Dbcp.Validation.FastFail failFast = validation.getFastFail();
        if (failFast != null) {
          dataSource.setFastFailValidation(true);
          if (failFast.getDisconnectionSqlCodes() != null && failFast.getDisconnectionSqlCodes().length() > 0) {
            if (disconnectionQueryCodes == null)
              disconnectionQueryCodes = new ArrayList<>();

            Collections.addAll(disconnectionQueryCodes, failFast.getDisconnectionSqlCodes().trim().split(" "));
          }
        }
      }

      final Dbcp.Logging logging = dbcp.getLogging();
      if (logging != null) {
        loggingLevel = logging.getLevel();
        if (logging.getLogExpiredConnections() != null)
          logExpiredConnections = logging.getLogExpiredConnections();

        if (logging.getLogAbandoned() != null)
          logAbandoned = logging.getLogAbandoned();
      }

      dataSource.setJmxName(dbcp.getJmxName());
    }
  }

  private DataSources(final String id, final ClassLoader driverClassLoader, final $Dbcp ... dbcps) {
    this.driverClassLoader = driverClassLoader;
    for (final $Dbcp dbcp : dbcps) {
      Assertions.assertNotNull(dbcp);
      if (id != null && (dbcp.getId$() == null || !id.equals(dbcp.getId$().text())))
        continue;

      if (dataSource == null)
        dataSource = new BasicDataSource();

      if (dbcp.getJdbc() != null) {
        if (dbcp.getJdbc().getDriverClassName() != null)
          driverClassName = dbcp.getJdbc().getDriverClassName().text();

        if (dbcp.getJdbc().getUrl() != null)
          url = dbcp.getJdbc().getUrl().text().toString();
      }

      final $Dbcp.Default _default = dbcp.getDefault();
      if (_default != null) {
        if (_default.getCatalog() != null)
          dataSource.setDefaultCatalog(_default.getCatalog().text());

        if (_default.getAutoCommit() != null)
          autoCommit = _default.getAutoCommit().text();

        if (_default.getReadOnly() != null)
          readOnly = _default.getReadOnly().text();

        if (_default.getQueryTimeout() != null)
          queryTimeout = _default.getQueryTimeout().text();

        if (_default.getTransactionIsolation() != null)
          transactionIsolation = _default.getTransactionIsolation().text();
      }

      final $Dbcp.Connection connection = dbcp.getConnection();
      if (connection != null) {
        if (connection.getProperties() != null)
          for (final $Dbcp.Connection.Properties.Property property : connection.getProperties().getProperty())
            if (property.getName$() != null && property.getValue$() != null)
              dataSource.addConnectionProperty(property.getName$().text(), property.getValue$().text());

        if (connection.getInitSqls() != null) {
          final List<$StringNonEmpty> initSqls = connection.getInitSqls().getInitSql();
          final String[] initSql = new String[initSqls.size()];
          for (int i = 0; i < initSqls.size(); ++i)
            initSql[i] = initSqls.get(i).text();

          if (dataSource.getConnectionInitSqls().size() > 0)
            Collections.addAll(dataSource.getConnectionInitSqls(), initSql);
          else
            dataSource.setConnectionInitSqls(Arrays.asList(initSql));
        }
      }

      final $Dbcp.Size size = dbcp.getSize();
      if (size != null) {
        if (size.getInitialSize() != null)
          initialSize = size.getInitialSize().text();

        if (size.getMinIdle() != null)
          minIdle = size.getMinIdle().text();

        if (size.getMaxIdle() != null)
          maxIdle = size.getMaxIdle().text();

        if (size.getMaxTotal() != null)
          maxTotal = size.getMaxTotal().text();

        if (size.getPoolPreparedStatements() != null) {
          poolPreparedStatements = true;
          if (size.getPoolPreparedStatements().getMaxOpen() != null)
            maxOpen = size.getPoolPreparedStatements().getMaxOpen().text();
        }
      }

      final $Dbcp.Pool pool = dbcp.getPool();
      if (pool != null) {
        final $Dbcp.Pool.Queue queue = pool.getQueue();
        if (queue != null) {
          if ("lifo".equals(queue.text()))
            lifo = true;
          else if ("fifo".equals(queue.text()))
            lifo = false;
          else
            throw new UnsupportedOperationException("Unsupported queue spec: " + queue);
        }

        if (pool.getCacheState() != null)
          cacheState = pool.getCacheState().text();

        if (pool.getMaxWait() != null)
          maxWait = pool.getMaxWait().text();

        if (pool.getMaxConnectionLifetime() != null)
          maxConnLifetime = pool.getMaxConnectionLifetime().text();

        if (pool.getAutoCommitOnReturn() != null)
          autoCommitOnReturn = pool.getAutoCommitOnReturn().text();

        if (pool.getRollbackOnReturn() != null)
          rollbackOnReturn = pool.getRollbackOnReturn().text();

        if (pool.getRemoveAbandoned() != null) {
          removeAbandonedOn = pool.getRemoveAbandoned().getOn$().text();
          removeAbandonedTimeout = pool.getRemoveAbandoned().getTimeout$().text();
        }

        if (pool.getAbandonedUsageTracking() != null)
          abandonedUsageTracking = pool.getAbandonedUsageTracking().text();

        if (pool.getAllowAccessToUnderlyingConnection() != null)
          accessToUnderlyingConnectionAllowed = pool.getAllowAccessToUnderlyingConnection().text();


        final $Dbcp.Pool.Eviction eviction = pool.getEviction();
        if (eviction != null) {
          hasEviction = true;
          if (eviction.getTimeBetweenRuns() != null)
            timeBetweenEvictionRunsMillis = eviction.getTimeBetweenRuns().text();

          if (eviction.getNumTestsPerRun() != null)
            numTestsPerRun = eviction.getNumTestsPerRun().text();

          if (eviction.getMinIdleTime() != null)
            minEvictableIdleTimeMillis = eviction.getMinIdleTime().text();

          if (eviction.getSoftMinIdleTime() != null)
            softMinEvictableIdleTimeMillis = eviction.getSoftMinIdleTime().text();

          if (eviction.getPolicyClassName() != null)
            policyClassName = eviction.getPolicyClassName().text();
        }
      }

      final $Dbcp.Validation validation = dbcp.getValidation();
      if (validation != null) {
        hasValidation = true;
        if (validation.getQuery() != null)
          validationQuery = validation.getQuery().text();

        if (validation.getTimeout() != null)
          validationQueryTimeout = validation.getTimeout().text();

        if (validation.getTestOnCreate() != null)
          testOnCreate = validation.getTestOnCreate().text();

        if (validation.getTestOnBorrow() != null)
          testOnBorrow = validation.getTestOnBorrow().text();

        if (validation.getTestOnReturn() != null)
          testOnReturn = validation.getTestOnReturn().text();

        if (validation.getTestWhileIdle() != null)
          testWhileIdle = validation.getTestWhileIdle().text();

        final $Dbcp.Validation.FastFail failFast = validation.getFastFail();
        if (failFast != null) {
          dataSource.setFastFailValidation(true);
          if (failFast.getDisconnectionSqlCodes() != null && failFast.getDisconnectionSqlCodes().text().length() > 0) {
            if (disconnectionQueryCodes == null)
              disconnectionQueryCodes = new ArrayList<>();

            Collections.addAll(disconnectionQueryCodes, failFast.getDisconnectionSqlCodes().text().trim().split(" "));
          }
        }
      }

      final $Dbcp.Logging logging = dbcp.getLogging();
      if (logging != null) {
        loggingLevel = logging.getLevel().text();
        if (logging.getLogExpiredConnections() != null)
          logExpiredConnections = logging.getLogExpiredConnections().text();

        if (logging.getLogAbandoned() != null)
          logAbandoned = logging.getLogAbandoned().text();
      }

      if (dbcp.getJmxName() != null)
        dataSource.setJmxName(dbcp.getJmxName().text());
    }
  }

  private BasicDataSource build() {
    if (dataSource == null)
      return null;

    if (driverClassName == null)
      throw new IllegalArgumentException("/dbcp:jdbc is missing");

    dataSource.setDriverClassName(driverClassName);
    dataSource.setDriverClassLoader(driverClassLoader);
    dataSource.setUrl(url);

    dataSource.setDefaultAutoCommit(autoCommit);
    dataSource.setDefaultReadOnly(readOnly);
    dataSource.setDefaultQueryTimeout(queryTimeout);

    if (transactionIsolation != null) {
      if ("NONE".equals(transactionIsolation))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
      else if ("READ_UNCOMMITTED".equals(transactionIsolation))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
      else if ("READ_COMMITTED".equals(transactionIsolation))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      else if ("REPEATABLE_READ".equals(transactionIsolation))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
      else if ("SERIALIZABLE".equals(transactionIsolation))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      else
        throw new UnsupportedOperationException("Unsupported transaction isolation: " + transactionIsolation);
    }

    dataSource.setInitialSize(initialSize);
    dataSource.setMinIdle(minIdle);
    dataSource.setMaxIdle(INDEFINITE.equals(maxIdle) ? DEFAULT_MAX_TOTAL : Integer.parseInt(maxIdle));
    dataSource.setMaxTotal(INDEFINITE.equals(maxTotal) ? DEFAULT_MAX_TOTAL : Integer.parseInt(maxTotal));
    dataSource.setPoolPreparedStatements(poolPreparedStatements);
    dataSource.setMaxOpenPreparedStatements(INDEFINITE.equals(maxOpen) ? DEFAULT_MAX_TOTAL : Integer.parseInt(maxOpen));

    dataSource.setLifo(lifo);

    dataSource.setCacheState(cacheState);
    dataSource.setMaxWaitMillis(INDEFINITE.equals(maxWait) ? DEFAULT_MAX_WAIT_MILLIS : Long.parseLong(maxWait));
    dataSource.setMaxConnLifetimeMillis(INDEFINITE.equals(maxConnLifetime) ? -1 : Long.parseLong(maxConnLifetime));
    dataSource.setAutoCommitOnReturn(autoCommitOnReturn);
    dataSource.setRollbackOnReturn(rollbackOnReturn);
    if (removeAbandonedOn != null) {
      if ("borrow".equals(removeAbandonedOn))
        dataSource.setRemoveAbandonedOnBorrow(true);
      else if ("maintenance".equals(removeAbandonedOn))
        dataSource.setRemoveAbandonedOnMaintenance(true);
      else
        throw new UnsupportedOperationException("Unsupported remove abandoned spec: " + removeAbandonedOn);

      dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
    }
    dataSource.setAbandonedUsageTracking(abandonedUsageTracking);
    dataSource.setAccessToUnderlyingConnectionAllowed(accessToUnderlyingConnectionAllowed);

    if (hasEviction) {
      dataSource.setTimeBetweenEvictionRunsMillis(INDEFINITE.equals(timeBetweenEvictionRunsMillis) ? DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS : Long.parseLong(timeBetweenEvictionRunsMillis));
      dataSource.setNumTestsPerEvictionRun(numTestsPerRun);
      dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
      dataSource.setSoftMinEvictableIdleTimeMillis(INDEFINITE.equals(softMinEvictableIdleTimeMillis) ? DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS : Long.parseLong(softMinEvictableIdleTimeMillis));
      if (policyClassName != null)
        dataSource.setEvictionPolicyClassName(policyClassName);
    }

    if (hasValidation) {
      if (validationQuery != null)
        dataSource.setValidationQuery(validationQuery);

      dataSource.setValidationQueryTimeout(INDEFINITE.equals(validationQueryTimeout) ? -1 : Integer.parseInt(validationQueryTimeout));
      dataSource.setTestOnCreate(testOnCreate);
      dataSource.setTestOnBorrow(testOnBorrow);
      dataSource.setTestOnReturn(testOnReturn);
      dataSource.setTestWhileIdle(testWhileIdle);

      if (dataSource.getFastFailValidation())
        dataSource.setDisconnectionSqlCodes(disconnectionQueryCodes != null ? disconnectionQueryCodes : defaultDisconnectionQueryCodes);
    }

    if (loggingLevel != null) {
      final Logger logger = LoggerFactory.getLogger(DataSources.class);
      final LoggerPrintWriter loggerPrintWriter = new LoggerPrintWriter(logger, Level.valueOf(loggingLevel));
      try {
        dataSource.setLogWriter(loggerPrintWriter);
      }
      catch (final SQLException e) {
        throw new RuntimeException(e); // Will not occur, because this behavior has been overridden.
      }

      dataSource.setLogExpiredConnections(logExpiredConnections);
      if (logAbandoned) {
        dataSource.setAbandonedLogWriter(loggerPrintWriter);
        dataSource.setLogAbandoned(true);
      }
    }

    return dataSource;
  }
}