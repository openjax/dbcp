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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.jaxsb.runtime.Bindings;
import org.junit.Test;
import org.libj.util.CollectionUtil;
import org.openjax.www.dbcp_1_2.xL0gluGCXAA.$Dbcp;
import org.xml.sax.SAXException;

public class DataSourcesTest {
  private static final Properties properties = new Properties();

  static {
    try (final InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("dbcp.properties")) {
      properties.load(in);
    }
    catch (final IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private static int getTransactionIsolation(final String transactionIsolation) {
    if ("NONE".equals(transactionIsolation))
      return Connection.TRANSACTION_NONE;

    if ("READ_UNCOMMITTED".equals(transactionIsolation))
      return Connection.TRANSACTION_READ_UNCOMMITTED;

    if ("READ_COMMITTED".equals(transactionIsolation))
      return Connection.TRANSACTION_READ_COMMITTED;

    if ("REPEATABLE_READ".equals(transactionIsolation))
      return Connection.TRANSACTION_REPEATABLE_READ;

    if ("SERIALIZABLE".equals(transactionIsolation))
      return Connection.TRANSACTION_SERIALIZABLE;

    throw new UnsupportedOperationException("Unsupported transaction isolation: " + transactionIsolation);
  }

  private static BasicDataSource validate(final BasicDataSource dataSource) {
    assertEquals(properties.get("driverClassName"), dataSource.getDriverClassName());
    assertEquals(properties.get("url"), dataSource.getUrl());
    assertEquals(properties.get("autoCommit"), "" + dataSource.getDefaultAutoCommit());
    assertEquals(properties.get("readOnly"), "" + dataSource.getDefaultReadOnly());
    assertEquals(properties.get("queryTimeout"), "" + dataSource.getDefaultQueryTimeout());
    assertEquals(getTransactionIsolation(properties.get("transactionIsolation").toString()), dataSource.getDefaultTransactionIsolation());
    assertArrayEquals(properties.get("initSql").toString().split(","), dataSource.getConnectionInitSqlsAsArray());
    assertEquals(properties.get("initialSize"), "" + dataSource.getInitialSize());
    assertEquals(properties.get("minIdle"), "" + dataSource.getMinIdle());
    assertEquals(properties.get("maxIdle"), "" + dataSource.getMaxIdle());
    assertEquals(properties.get("maxTotal"), "" + dataSource.getMaxTotal());
    assertEquals(properties.get("poolPreparedStatements"), "" + dataSource.isPoolPreparedStatements());
    assertEquals(properties.get("maxOpen").equals("INDEFINITE"), dataSource.getMaxOpenPreparedStatements() == -1);
    assertEquals(properties.get("queue").equals("lifo"), dataSource.getLifo());
    assertEquals(properties.get("cacheState"), "" + dataSource.getCacheState());
    assertEquals(properties.get("maxWait").equals("INDEFINITE"), dataSource.getMaxWaitMillis() == -1);
    assertEquals(properties.get("maxConnectionLifetime").equals("INDEFINITE"), dataSource.getMaxConnLifetimeMillis() == -1);
    assertEquals(properties.get("autoCommitOnReturn"), "" + dataSource.getAutoCommitOnReturn());
    assertEquals(properties.get("rollbackOnReturn"), "" + dataSource.getRollbackOnReturn());
    assertEquals(properties.get("removeAbandonedOn").equals("maintenance"), dataSource.getRemoveAbandonedOnMaintenance());
    assertEquals(properties.get("removeAbandonedOn").equals("borrow"), dataSource.getRemoveAbandonedOnBorrow());
    assertEquals(properties.get("removeAbandonedTimeout"), "" + dataSource.getRemoveAbandonedTimeout());
    assertEquals(properties.get("abandonedUsageTracking"), "" + dataSource.getAbandonedUsageTracking());
    assertEquals(properties.get("allowAccessToUnderlyingConnection"), "" + dataSource.isAccessToUnderlyingConnectionAllowed());
    assertEquals(properties.get("timeBetweenRuns"), "" + dataSource.getTimeBetweenEvictionRunsMillis());
    assertEquals(properties.get("numTestsPerRun"), "" + dataSource.getNumTestsPerEvictionRun());
    assertEquals(properties.get("minIdleTime"), "" + dataSource.getMinEvictableIdleTimeMillis());
    assertEquals(properties.get("softMinIdleTime").equals("INDEFINITE"), dataSource.getSoftMinEvictableIdleTimeMillis() == -1);
    assertEquals(properties.get("policyClassName"), "" + dataSource.getEvictionPolicyClassName());
    assertEquals(properties.get("query"), dataSource.getValidationQuery());
    assertEquals(properties.get("testOnCreate"), "" + dataSource.getTestOnCreate());
    assertEquals(properties.get("testOnBorrow"), "" + dataSource.getTestOnBorrow());
    assertEquals(properties.get("testOnReturn"), "" + dataSource.getTestOnReturn());
    assertEquals(properties.get("testWhileIdle"), "" + dataSource.getTestWhileIdle());
    assertEquals(properties.get("timeout").equals("INDEFINITE"), dataSource.getValidationQueryTimeout() == -1);
    assertEquals(CollectionUtil.asCollection(new HashSet<>(), properties.get("disconnectionSqlCodes").toString().split(" ")), dataSource.getDisconnectionSqlCodes());
    assertEquals(properties.get("logExpiredConnections"), "" + dataSource.getLogExpiredConnections());
    assertEquals(properties.get("logAbandoned"), "" + dataSource.getLogAbandoned());
    assertEquals(properties.get("jmxName"), dataSource.getJmxName());
    return dataSource;
  }

  @Test
  public void testJaxb() throws IOException, SAXException, SQLException {
    try (
      final BasicDataSource dataSource = validate(DataSources.createDataSource(ClassLoader.getSystemClassLoader().getResource("dbcp.xml")));
      final Connection connection = dataSource.getConnection();
    ) {
      try (
        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
      ) {
        assertTrue(resultSet.next());
        assertEquals(1, resultSet.getInt(1));
        assertFalse(resultSet.next());
      }
    }

    new File("derby.log").delete();
  }

  @Test
  public void testXsb() throws IOException, SAXException, SQLException {
    final $Dbcp dbcp = ($Dbcp)Bindings.parse(ClassLoader.getSystemClassLoader().getResource("dbcp.xml"));
    try (
      final BasicDataSource dataSource = validate(DataSources.createDataSource(dbcp));
      final Connection connection = dataSource.getConnection();
    ) {
      try (
        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
      ) {
        assertTrue(resultSet.next());
        assertEquals(1, resultSet.getInt(1));
        assertFalse(resultSet.next());
      }
    }

    new File("derby.log").delete();
  }
}