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

package org.openjax.support.dbcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
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

import org.apache.commons.dbcp2.BasicDataSource;
import org.openjax.standard.logging.LoggerPrintWriter;
import org.openjax.support.dbcp_1_0_4.Dbcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.xml.sax.SAXException;

public final class DataSources {
  private static final String INDEFINITE = "INDEFINITE";
  private static Schema schema;

  /**
   * Create a {@code BasicDataSource} given a dbcp JAXB binding.
   * {@code ClassLoader.getSystemClassLoader()} is used as the
   * {@code driverClassLoader} parameter.
   *
   * @param dbcpXml URL of dbcp xml resource.
   * @return The {@code BasicDataSource} instance.
   * @throws SAXException If a XML validation error occurs.
   * @throws SQLException If a database access error occurs.
   * @throws IOException If an IO exception occurs.
   */
  public static BasicDataSource createDataSource(final URL dbcpXml) throws IOException, SAXException, SQLException {
    return createDataSource(dbcpXml, ClassLoader.getSystemClassLoader());
  }

  /**
   * Create a {@code BasicDataSource} given a dbcp JAXB binding.
   *
   * @param dbcpXml URL of dbcp xml resource.
   * @param driverClassLoader Class loader to be used to load the JDBC driver.
   * @return The {@code BasicDataSource} instance.
   * @throws SAXException If a XML validation error occurs.
   * @throws SQLException If a database access error occurs.
   * @throws IOException If an IO exception occurs.
   */
  public static BasicDataSource createDataSource(final URL dbcpXml, final ClassLoader driverClassLoader) throws IOException, SAXException, SQLException {
    try {
      final Unmarshaller unmarshaller = JAXBContext.newInstance(Dbcp.class).createUnmarshaller();
      unmarshaller.setSchema(DataSources.schema == null ? DataSources.schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(Thread.currentThread().getContextClassLoader().getResource("dbcp.xsd")) : DataSources.schema);

      try (final InputStream in = dbcpXml.openStream()) {
        final JAXBElement<Dbcp> element = unmarshaller.unmarshal(XMLInputFactory.newInstance().createXMLStreamReader(in), Dbcp.class);
        return createDataSource(element.getValue(), driverClassLoader);
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
   * Create a {@code BasicDataSource} given a list of dbcp XSB bindings.
   * {@code ClassLoader.getSystemClassLoader()} is used as the
   * {@code driverClassLoader} parameter.
   *
   * @param dbcps List of {@code Dbcp} descriptor objects.
   * @param name The name of the pool to create. (The name is declared in the
   *          list of {@code dbcps}).
   * @return The {@code BasicDataSource} instance.
   * @throws SQLException If a database access error occurs.
   */
  public static BasicDataSource createDataSource(final List<Dbcp> dbcps, final String name) throws SQLException {
    return createDataSource(dbcps, name, ClassLoader.getSystemClassLoader());
  }

  /**
   * Create a {@code BasicDataSource} given a list of dbcp XSB bindings.
   *
   * @param dbcps List of {@code Dbcp} descriptor objects.
   * @param name The name of the pool to create. (The name is declared in the
   *          list of {@code dbcps}).
   * @param driverClassLoader Class loader to be used to load the JDBC driver.
   * @return The {@code BasicDataSource} instance.
   * @throws SQLException If a database access error occurs.
   */
  public static BasicDataSource createDataSource(final List<Dbcp> dbcps, final String name, final ClassLoader driverClassLoader) throws SQLException {
    for (final Dbcp dbcp : dbcps)
      if (name.equals(dbcp.getName()))
        return createDataSource(dbcp, driverClassLoader);

    return null;
  }

  /**
   * Create a {@code BasicDataSource} given a dbcp JAXB binding.
   *
   * @param dbcp The {@code Dbcp} descriptor object.
   * @return The {@code BasicDataSource} instance.
   * @throws SQLException If a database access error occurs.
   */
  public static BasicDataSource createDataSource(final Dbcp dbcp) throws SQLException {
    return createDataSource(dbcp, ClassLoader.getSystemClassLoader());
  }

  /**
   * Create a {@code BasicDataSource} given a dbcp JAXB binding.
   *
   * @param dbcp The {@code Dbcp} descriptor object.
   * @param driverClassLoader Class loader to be used to load the JDBC driver.
   * @return The {@code BasicDataSource} instance.
   * @throws SQLException If a database access error occurs.
   */
  public static BasicDataSource createDataSource(final Dbcp dbcp, final ClassLoader driverClassLoader) throws SQLException {
    final BasicDataSource dataSource = new BasicDataSource();

    final Dbcp.Jdbc jdbc = dbcp.getJdbc();
    dataSource.setDriverClassName(jdbc.getDriverClassName());
    dataSource.setDriverClassLoader(driverClassLoader);

    dataSource.setUrl(jdbc.getUrl());

    dataSource.setUsername(jdbc.getUsername());
    dataSource.setPassword(jdbc.getPassword());

    final Dbcp.Default _default = dbcp.getDefault();
    if (_default != null && _default.getCatalog() != null)
      dataSource.setDefaultCatalog(_default.getCatalog());

    dataSource.setDefaultAutoCommit(_default == null || _default.getAutoCommit() == null || _default.getAutoCommit());
    dataSource.setDefaultReadOnly(_default != null && _default.getReadOnly() != null && _default.getReadOnly());
    if (_default != null && _default.getQueryTimeout() != null)
      dataSource.setDefaultQueryTimeout(_default.getQueryTimeout());

    if (_default != null && _default.getTransactionIsolation() != null) {
      if ("NONE".equals(_default.getTransactionIsolation()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
      else if ("READ_COMMITTED".equals(_default.getTransactionIsolation()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      else if ("READ_UNCOMMITTED".equals(_default.getTransactionIsolation()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
      else if ("REPEATABLE_READ".equals(_default.getTransactionIsolation()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
      else if ("SERIALIZABLE".equals(_default.getTransactionIsolation()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      else
        throw new UnsupportedOperationException("Unsupported transaction isolation: " + _default.getTransactionIsolation());
    }

    final Dbcp.Connection connection = dbcp.getConnection();
    if (connection != null) {
      if (connection.getProperties() != null)
        for (final Dbcp.Connection.Properties.Property property : connection.getProperties().getProperty())
          if (property.getName() != null && property.getValue() != null)
            dataSource.addConnectionProperty(property.getName(), property.getValue());

      if (connection.getInitSqls() != null)
        dataSource.setConnectionInitSqls(connection.getInitSqls().getInitSql());
    }

    final Dbcp.Size size = dbcp.getSize();
    dataSource.setInitialSize(size == null || size.getInitialSize() == null ? 0 : size.getInitialSize());
    dataSource.setMaxTotal(size == null || size.getMaxTotal() == null ? 8 : INDEFINITE.equals(size.getMaxTotal()) ? -1 : Integer.parseInt(size.getMaxTotal()));
    dataSource.setMaxIdle(size == null || size.getMaxIdle() == null ? 8 : INDEFINITE.equals(size.getMaxIdle()) ? -1 : Integer.parseInt(size.getMaxIdle()));
    dataSource.setMinIdle(size == null || size.getMinIdle() == null ? 9 : size.getMinIdle());
    if (size == null || size.getMaxOpenPreparedStatements() == null || INDEFINITE.equals(size.getMaxOpenPreparedStatements())) {
      dataSource.setPoolPreparedStatements(false);
    }
    else {
      dataSource.setPoolPreparedStatements(true);
      dataSource.setMaxOpenPreparedStatements(Integer.parseInt(size.getMaxOpenPreparedStatements()));
    }

    final Dbcp.Pool pool = dbcp.getPool();
    if (pool == null || pool.getQueue() == null || "lifo".equals(pool.getQueue()))
      dataSource.setLifo(true);
    else if ("fifo".equals(pool.getQueue()))
      dataSource.setLifo(false);
    else
      throw new UnsupportedOperationException("Unsupported queue spec: " + pool.getQueue());

    dataSource.setCacheState(pool != null && pool.getCacheState() != null && pool.getCacheState());
    dataSource.setMaxWaitMillis(pool == null || pool.getMaxWait() != null || INDEFINITE.equals(pool.getMaxWait()) ? -1 : Long.parseLong(pool.getMaxWait()));
    dataSource.setMaxConnLifetimeMillis(pool == null || pool.getMaxConnectionLifetime() == null || INDEFINITE.equals(pool.getMaxConnectionLifetime()) ? 0 : Long.parseLong(pool.getMaxConnectionLifetime()));
    dataSource.setEnableAutoCommitOnReturn(_default == null || pool.getEnableAutoCommitOnReturn() == null || pool.getEnableAutoCommitOnReturn());
    dataSource.setRollbackOnReturn(pool == null || pool.getRollbackOnReturn() == null || pool.getRollbackOnReturn());
    if (pool != null && pool.getRemoveAbandoned() != null) {
      if ("borrow".equals(pool.getRemoveAbandoned().getOn()))
        dataSource.setRemoveAbandonedOnBorrow(true);
      else if ("maintenance".equals(pool.getRemoveAbandoned().getOn()))
        dataSource.setRemoveAbandonedOnMaintenance(true);
      else
        throw new UnsupportedOperationException("Unsupported remove abandoned spec: " + pool.getRemoveAbandoned().getOn());

      dataSource.setRemoveAbandonedTimeout(pool.getRemoveAbandoned().getTimeout());
    }

    dataSource.setAbandonedUsageTracking(pool != null && pool.getAbandonedUsageTracking() != null && pool.getAbandonedUsageTracking());
    dataSource.setAccessToUnderlyingConnectionAllowed(pool != null && pool.getAllowAccessToUnderlyingConnection() != null && pool.getAllowAccessToUnderlyingConnection());

    final Dbcp.Pool.Eviction evictor = pool != null && pool.getEviction() != null ? pool.getEviction() : null;
    if (evictor != null) {
      dataSource.setTimeBetweenEvictionRunsMillis(evictor.getTimeBetweenRuns());
      dataSource.setNumTestsPerEvictionRun(evictor.getNumTestsPerRun());
      dataSource.setMinEvictableIdleTimeMillis(evictor.getMinIdleTime() == null ? 1800000 : evictor.getMinIdleTime());
      dataSource.setSoftMinEvictableIdleTimeMillis(evictor.getSoftMinIdleTime() == null || INDEFINITE.equals(evictor.getSoftMinIdleTime()) ? -1 : Long.parseLong(evictor.getSoftMinIdleTime()));
      if (evictor.getPolicyClassName() != null)
        dataSource.setEvictionPolicyClassName(evictor.getPolicyClassName());
    }

    final Dbcp.Validation validation = dbcp.getValidation();
    if (validation != null && validation.getQuery() != null)
      dataSource.setValidationQuery(validation.getQuery());

    dataSource.setTestOnBorrow(validation == null || validation.getTestOnBorrow() == null || validation.getTestOnBorrow());
    dataSource.setTestOnReturn(validation != null && validation.getTestOnReturn() != null && validation.getTestOnReturn());
    dataSource.setTestWhileIdle(validation != null && validation.getTestWhileIdle() != null && validation.getTestWhileIdle());
    if (validation != null && validation.getFastFail() != null) {
      dataSource.setFastFailValidation(true);
      if (validation.getFastFail().getDisconnectionSqlCodes() != null)
        dataSource.setDisconnectionSqlCodes(Arrays.asList(validation.getFastFail().getDisconnectionSqlCodes().split(" ")));
    }

    final Dbcp.Logging logging = dbcp.getLogging();
    if (logging != null) {
      final Logger logger = LoggerFactory.getLogger(DataSources.class);
      final LoggerPrintWriter loggerPrintWriter = new LoggerPrintWriter(logger, Level.valueOf(logging.getLevel().toString()));
      dataSource.setLogWriter(loggerPrintWriter);
      dataSource.setLogExpiredConnections(logging.isLogExpiredConnections());
      if (logging.isLogAbandoned()) {
        dataSource.setAbandonedLogWriter(loggerPrintWriter);
        dataSource.setLogAbandoned(true);
      }
    }

    return dataSource;
  }

  private DataSources() {
  }
}