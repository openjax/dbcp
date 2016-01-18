package org.safris.commons.dbcp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.safris.xml.generator.compiler.runtime.BindingRuntimeException;

public final class DataSources {
  public static BasicDataSource createDataSource(final List<$dbcp_dbcp> dbcps, final String name) throws SQLException {
    for (final $dbcp_dbcp dbcp : dbcps)
      if (name.equals(dbcp._name$().text()))
        return createDataSource(dbcp);

    return null;
  }

  public static BasicDataSource createDataSource(final $dbcp_dbcp dbcp) throws SQLException {
    if (dbcp.isNull())
      throw new BindingRuntimeException("/dbcp:jdbc is missing");

    final dbcp_dbcp._jdbc jdbc = dbcp._jdbc(0);
    final BasicDataSource dataSource = new BasicDataSource();
    if (jdbc._driverClassName() == null || jdbc._driverClassName().size() == 0 || jdbc._driverClassName(0).text() == null)
      throw new BindingRuntimeException("/dbcp:jdbc/dbcp:driverClassName is missing");

    dataSource.setDriverClassName(jdbc._driverClassName(0).text());

//    if(jdbc._loginTimeout() != null && jdbc._loginTimeout().size() != 0 && jdbc._loginTimeout(0).text() != null) {
// FIXME: This causes a ClassNotFoundException: com.sybase.jdbc3.jdbc.SybDriver
//      try {
//        dataSource.setLoginTimeout(jdbc._loginTimeout(0).text());
//      }
//      catch(final SQLException e) {
//        throw new SQLException(e);
//      }
//  }

    if (jdbc._url() == null || jdbc._url().size() == 0 || jdbc._url(0).text() == null)
      throw new BindingRuntimeException("/dbcp:jdbc/dbcp:url is missing");

    dataSource.setUrl(jdbc._url(0).text());

    if (jdbc._username() == null || jdbc._username().size() == 0 || jdbc._username(0).text() == null)
      throw new BindingRuntimeException("/dbcp:jdbc/dbcp:username is missing");

    dataSource.setUsername(jdbc._username(0).text());

    if (jdbc._password() == null || jdbc._password().size() == 0 || jdbc._password(0).text() == null)
      throw new BindingRuntimeException("/dbcp:jdbc/dbcp:password is missing");

    dataSource.setPassword(jdbc._password(0).text());

    final dbcp_dbcp._default defaults = dbcp._default(0);
    if (defaults._connectionProperties() != null && defaults._connectionProperties().size() != 0 && defaults._connectionProperties(0)._property() != null && defaults._connectionProperties(0)._property().size() != 0)
      for (final dbcp_dbcp._default._connectionProperties._property property : defaults._connectionProperties(0)._property())
        if (property._name$() != null && property._name$().text() != null && property._value$() != null && property._value$().text() != null)
          dataSource.addConnectionProperty(property._name$().text(), property._value$().text());

    if (defaults._autoCommit() != null && defaults._autoCommit().size() != 0 && defaults._autoCommit(0).text() != null)
      dataSource.setDefaultAutoCommit(defaults._autoCommit(0).text());

    if (defaults._readOnly() != null && defaults._readOnly().size() != 0 && defaults._readOnly(0).text() != null)
      dataSource.setDefaultReadOnly(defaults._readOnly(0).text());

    if (defaults._transactionIsolation() != null && defaults._transactionIsolation().size() != 0 && defaults._transactionIsolation(0).text() != null) {
      if (dbcp_dbcp._default._transactionIsolation.NONE.text().equals(defaults._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
      else if (dbcp_dbcp._default._transactionIsolation.READ_5FCOMMITTED.text().equals(defaults._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      else if (dbcp_dbcp._default._transactionIsolation.READ_5FUNCOMMITTED.text().equals(defaults._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
      else if (dbcp_dbcp._default._transactionIsolation.REPEATABLE_5FREAD.text().equals(defaults._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
      else if (dbcp_dbcp._default._transactionIsolation.SERIALIZABLE.text().equals(defaults._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }

    if (dbcp._size() != null && dbcp._size().size() != 0) {
      final dbcp_dbcp._size size = dbcp._size(0);

      if (!size._initialSize(0).isNull())
        dataSource.setInitialSize(size._initialSize(0).text().intValue());

      if (!size._maxActive(0).isNull())
        dataSource.setMaxActive(size._maxActive(0).text().intValue());

      if (!size._maxIdle(0).isNull())
        dataSource.setMaxIdle(size._maxIdle(0).text().intValue());

      if (!size._minIdle(0).isNull())
        dataSource.setMinIdle(size._minIdle(0).text().intValue());

      if (!size._maxWait(0).isNull())
        dataSource.setMaxWait(size._maxWait(0).text().intValue());
    }

    if (dbcp._management() != null && dbcp._management().size() != 0) {
      final dbcp_dbcp._management management = dbcp._management(0);

      if (!management._validationQuery(0).isNull())
        dataSource.setValidationQuery(management._validationQuery(0).text());

      if (!management._testOnBorrow(0).isNull())
        dataSource.setTestOnBorrow(management._testOnBorrow(0).text());

      if (!management._testOnReturn(0).isNull())
        dataSource.setTestOnReturn(management._testOnReturn(0).text());

      if (!management._testWhileIdle(0).isNull())
        dataSource.setTestWhileIdle(management._testWhileIdle(0).text());

      if (!management._timeBetweenEvictionRuns(0).isNull())
        dataSource.setTimeBetweenEvictionRunsMillis(management._timeBetweenEvictionRuns(0).text().intValue());

      if (!management._numTestsPerEvictionRun(0).isNull())
        dataSource.setNumTestsPerEvictionRun(management._numTestsPerEvictionRun(0).text().intValue());

      if (!management._minEvictableIdleTime(0).isNull())
        dataSource.setMinEvictableIdleTimeMillis(management._minEvictableIdleTime(0).text().intValue());
    }

    if (dbcp._preparedStatements() != null && dbcp._preparedStatements().size() != 0) {
      final dbcp_dbcp._preparedStatements preparedStatements = dbcp._preparedStatements(0);

      if (!preparedStatements._poolPreparedStatements(0).isNull())
        dataSource.setPoolPreparedStatements(preparedStatements._poolPreparedStatements(0).text());

      if (!preparedStatements._maxOpenPreparedStatements(0).isNull())
        dataSource.setMaxOpenPreparedStatements(preparedStatements._maxOpenPreparedStatements(0).text().intValue());
    }

    if (dbcp._removal() != null && dbcp._removal().size() != 0) {
      final dbcp_dbcp._removal removal = dbcp._removal(0);

      if (!removal._removeAbandoned(0).isNull())
        dataSource.setRemoveAbandoned(removal._removeAbandoned(0).text());

      if (!removal._removeAbandonedTimeout(0).isNull())
        dataSource.setRemoveAbandonedTimeout(removal._removeAbandonedTimeout(0).text().intValue());

      if (!removal._logAbandoned(0).isNull())
        dataSource.setLogAbandoned(removal._logAbandoned(0).text());
    }

    return dataSource;
  }

  private DataSources() {
  }
}