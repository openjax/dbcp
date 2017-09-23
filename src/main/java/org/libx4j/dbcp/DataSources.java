/* Copyright (c) 2008 lib4j
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

package org.libx4j.dbcp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.lib4j.logging.LoggerPrintWriter;
import org.lib4j.xml.datatypes.xe.$dt_stringNonEmpty;
import org.libx4j.dbcp.xe.$dbcp_dbcp;
import org.libx4j.dbcp.xe.dbcp_dbcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public final class DataSources {
  private static final String INDEFINITE = "INDEFINITE";

  /**
   * Create a <code>BasicDataSource</code> given a list of dbcp XSB bindings.
   * <code>ClassLoader.getSystemClassLoader()</code> is used as the <code>driverClassLoader</code> parameter.
   *
   * @param dbcp The XSB binding.
   * @param name The name of the pool to create. (The name is declared in the list of <code>dbcps</code>).
   * @return the <code>BasicDataSource</code> instance.
   * @throws SQLException If a database access error occurs.
   */
  public static BasicDataSource createDataSource(final List<$dbcp_dbcp> dbcps, final String name) throws SQLException {
    return createDataSource(dbcps, name, ClassLoader.getSystemClassLoader());
  }

  /**
   * Create a <code>BasicDataSource</code> given a list of dbcp XSB bindings.
   *
   * @param dbcp The XSB binding.
   * @param name The name of the pool to create. (The name is declared in the list of <code>dbcps</code>).
   * @param driverClassLoader Class loader to be used to load the JDBC driver.
   * @return the <code>BasicDataSource</code> instance.
   * @throws SQLException If a database access error occurs.
   */
  public static BasicDataSource createDataSource(final List<$dbcp_dbcp> dbcps, final String name, final ClassLoader driverClassLoader) throws SQLException {
    for (final $dbcp_dbcp dbcp : dbcps)
      if (name.equals(dbcp._name$().text()))
        return createDataSource(dbcp, driverClassLoader);

    return null;
  }

  /**
   * Create a <code>BasicDataSource</code> given a dbcp XSB binding.
   * <code>ClassLoader.getSystemClassLoader()</code> is used as the <code>driverClassLoader</code> parameter.
   *
   * @param dbcp The XSB binding.
   * @return the <code>BasicDataSource</code> instance.
   * @throws SQLException If a database access error occurs.
   */
  public static BasicDataSource createDataSource(final $dbcp_dbcp dbcp) throws SQLException {
    return createDataSource(dbcp, ClassLoader.getSystemClassLoader());
  }

  /**
   * Create a <code>BasicDataSource</code> given a dbcp XSB binding.
   *
   * @param dbcp The XSB binding.
   * @param driverClassLoader Class loader to be used to load the JDBC driver.
   * @return the <code>BasicDataSource</code> instance.
   * @throws SQLException If a database access error occurs.
   */
  public static BasicDataSource createDataSource(final $dbcp_dbcp dbcp, final ClassLoader driverClassLoader) throws SQLException {
    if (dbcp.isNull())
      throw new IllegalArgumentException("dbcp.isNull() == true");

    final BasicDataSource dataSource = new BasicDataSource();

    final dbcp_dbcp._jdbc jdbc = dbcp._jdbc(0);
    if (jdbc.isNull())
      throw new IllegalArgumentException("Missing required value for '/dbcp:jdbc'");

    if (jdbc._driverClassName(0).isNull())
      throw new IllegalArgumentException("Missing required value for '/dbcp:jdbc/dbcp:driverClassName'");

    dataSource.setDriverClassName(jdbc._driverClassName(0).text());
    dataSource.setDriverClassLoader(driverClassLoader);

    if (jdbc._url(0).isNull())
      throw new IllegalArgumentException("Missing required value for '/dbcp:jdbc/dbcp:url'");

    dataSource.setUrl(jdbc._url(0).text());

    if (jdbc._username(0).isNull())
      throw new IllegalArgumentException("Missing required value for '/dbcp:jdbc/dbcp:username'");

    dataSource.setUsername(jdbc._username(0).text());

    if (jdbc._password(0).isNull())
      throw new IllegalArgumentException("Missing required value for '/dbcp:jdbc/dbcp:password'");

    dataSource.setPassword(jdbc._password(0).text());

    final dbcp_dbcp._default _default = dbcp._default(0);
    if (!_default.isNull() && !_default._catalog(0).isNull())
      dataSource.setDefaultCatalog(_default._catalog(0).text());

    dataSource.setDefaultAutoCommit(_default.isNull() || _default._autoCommit(0).isNull() || _default._autoCommit(0).text());
    dataSource.setDefaultReadOnly(!_default.isNull() && !_default._readOnly(0).isNull() && _default._readOnly(0).text());
    if (!_default.isNull() && !_default._queryTimeout(0).isNull())
      dataSource.setDefaultQueryTimeout(_default._queryTimeout(0).text());

    if (!_default.isNull() && !_default._transactionIsolation(0).isNull()) {
      if (dbcp_dbcp._default._transactionIsolation.NONE.text().equals(_default._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
      else if (dbcp_dbcp._default._transactionIsolation.READ_5FCOMMITTED.text().equals(_default._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      else if (dbcp_dbcp._default._transactionIsolation.READ_5FUNCOMMITTED.text().equals(_default._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
      else if (dbcp_dbcp._default._transactionIsolation.REPEATABLE_5FREAD.text().equals(_default._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
      else if (dbcp_dbcp._default._transactionIsolation.SERIALIZABLE.text().equals(_default._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      else
        throw new UnsupportedOperationException("Unsupported transaction isolation: " + _default._transactionIsolation(0).text());
    }

    final dbcp_dbcp._connection connection = dbcp._connection(0);
    if (!connection.isNull()) {
      if (!connection._properties(0).isNull())
        for (final dbcp_dbcp._connection._properties._property property : connection._properties(0)._property())
          if (property._name$() != null && property._name$().text() != null && property._value$() != null && property._value$().text() != null)
            dataSource.addConnectionProperty(property._name$().text(), property._value$().text());

      if (!connection._initSqls(0).isNull()) {
        final List<String> initSqls = new ArrayList<String>();
        for (final $dt_stringNonEmpty initSql : connection._initSqls(0)._initSql())
          initSqls.add(initSql.text());

        dataSource.setConnectionInitSqls(initSqls);
      }
    }

    final dbcp_dbcp._size size = dbcp._size(0);
    dataSource.setInitialSize(size.isNull() || size._initialSize(0).isNull() ? 0 : size._initialSize(0).text().intValue());
    dataSource.setMaxTotal(size.isNull() || size._maxTotal(0).isNull() ? 8 : INDEFINITE.equals(size._maxTotal(0).text()) ? -1 : Integer.parseInt(size._maxTotal(0).text()));
    dataSource.setMaxIdle(size.isNull() || size._maxIdle(0).isNull() ? 8 : INDEFINITE.equals(size._maxIdle(0).text()) ? -1 : Integer.parseInt(size._maxIdle(0).text()));
    dataSource.setMinIdle(size.isNull() || size._minIdle(0).isNull() ? 9 : size._minIdle(0).text().intValue());
    if (size.isNull() || size._maxOpenPreparedStatements(0).isNull() || INDEFINITE.equals(size._maxOpenPreparedStatements(0).text())) {
      dataSource.setPoolPreparedStatements(false);
    }
    else {
      dataSource.setPoolPreparedStatements(true);
      dataSource.setMaxOpenPreparedStatements(Integer.parseInt(size._maxOpenPreparedStatements(0).text()));
    }

    final dbcp_dbcp._pool pool = dbcp._pool(0);
    if (pool.isNull() || pool._queue(0).isNull() || "lifo".equals(pool._queue(0).text()))
      dataSource.setLifo(true);
    else if ("fifo".equals(pool._queue(0).text()))
      dataSource.setLifo(false);
    else
      throw new UnsupportedOperationException("Unsupported queue spec: " + pool._queue(0).text());

    dataSource.setCacheState(!pool.isNull() && !pool._cacheState(0).isNull() && pool._cacheState(0).text());
    dataSource.setMaxWaitMillis(pool.isNull() || pool._maxWait(0).isNull() || INDEFINITE.equals(pool._maxWait(0).text()) ? -1 : Long.parseLong(pool._maxWait(0).text()));
    dataSource.setMaxConnLifetimeMillis(pool.isNull() || pool._maxConnectionLifetime(0).isNull() || INDEFINITE.equals(pool._maxConnectionLifetime(0).text()) ? 0 : Long.parseLong(pool._maxConnectionLifetime(0).text()));
    dataSource.setEnableAutoCommitOnReturn(_default.isNull() || pool._enableAutoCommitOnReturn(0).isNull() || pool._enableAutoCommitOnReturn(0).text());
    dataSource.setRollbackOnReturn(pool.isNull() || pool._rollbackOnReturn(0).isNull() || pool._rollbackOnReturn(0).text());
    if (!pool.isNull() && !pool._removeAbandoned(0).isNull()) {
      if ("borrow".equals(pool._removeAbandoned(0)._on$().text()))
        dataSource.setRemoveAbandonedOnBorrow(true);
      else if ("maintenance".equals(pool._removeAbandoned(0)._on$().text()))
        dataSource.setRemoveAbandonedOnMaintenance(true);
      else
        throw new UnsupportedOperationException("Unsupported remove abandoned spec: " + pool._removeAbandoned(0)._on$().text());

      dataSource.setRemoveAbandonedTimeout(pool._removeAbandoned(0)._timeout$().text());
    }

    dataSource.setAbandonedUsageTracking(!pool.isNull() && !pool._abandonedUsageTracking(0).isNull() && pool._abandonedUsageTracking(0).text());
    dataSource.setAccessToUnderlyingConnectionAllowed(!pool.isNull() && !pool._allowAccessToUnderlyingConnection(0).isNull() && pool._allowAccessToUnderlyingConnection(0).text());

    final dbcp_dbcp._pool._evictor evictor = !pool.isNull() && !pool._evictor(0).isNull() ? pool._evictor(0) : null;
    if (evictor != null) {
      dataSource.setTimeBetweenEvictionRunsMillis(evictor._timeBetweenRuns(0).text());
      dataSource.setNumTestsPerEvictionRun(evictor._numTestsPerRun(0).text());
      dataSource.setMinEvictableIdleTimeMillis(evictor._minIdleTime(0).isNull() ? 1800000 : evictor._minIdleTime(0).text());
      dataSource.setSoftMinEvictableIdleTimeMillis(evictor._softMinIdleTime(0).isNull() || INDEFINITE.equals(evictor._softMinIdleTime(0).text()) ? -1 : Long.parseLong(evictor._softMinIdleTime(0).text()));
      if (!evictor._policyClassName(0).isNull())
        dataSource.setEvictionPolicyClassName(evictor._policyClassName(0).text());
    }

    final dbcp_dbcp._validation validation = dbcp._validation(0);
    if (!validation.isNull() && !validation._query(0).isNull())
      dataSource.setValidationQuery(validation._query(0).text());

    dataSource.setTestOnBorrow(validation.isNull() || validation._testOnBorrow(0).isNull() || validation._testOnBorrow(0).text());
    dataSource.setTestOnReturn(!validation.isNull() && !validation._testOnReturn(0).isNull() && validation._testOnReturn(0).text());
    dataSource.setTestWhileIdle(!validation.isNull() && !validation._testWhileIdle(0).isNull() && validation._testWhileIdle(0).text());
    if (!validation.isNull() && !validation._fastFail(0).isNull()) {
      dataSource.setFastFailValidation(true);
      if (!validation._fastFail(0)._disconnectionSqlCodes(0).isNull())
        dataSource.setDisconnectionSqlCodes(Arrays.asList(validation._fastFail(0)._disconnectionSqlCodes(0).text().split(" ")));
    }

    final dbcp_dbcp._logging logging = dbcp._logging(0);
    if (!logging.isNull()) {
      final Logger logger = LoggerFactory.getLogger(DataSources.class);
      final LoggerPrintWriter loggerPrintWriter = new LoggerPrintWriter(logger, Level.valueOf(logging._level(0).text()));
      dataSource.setLogWriter(loggerPrintWriter);
      dataSource.setLogExpiredConnections(!logging._logExpiredConnections(0).isNull() && logging._logExpiredConnections(0).text());
      if (!logging._logAbandoned(0).isNull() && logging._logAbandoned(0).text()) {
        dataSource.setAbandonedLogWriter(loggerPrintWriter);
        dataSource.setLogAbandoned(true);
      }
    }

    return dataSource;
  }

  private DataSources() {
  }
}