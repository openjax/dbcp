package org.safris.commons.dbcp;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.safris.xml.schema.binding.dbcp.$dbcp_dbcpType;

public final class DataSources {
  public static BasicDataSource createDataSource(final $dbcp_dbcpType dbcpType) throws SQLException {
    if (dbcpType._jdbc() == null || dbcpType._jdbc().size() == 0)
      throw new SQLException("jdbc is not present");

    final $dbcp_dbcpType._jdbc jdbc = dbcpType._jdbc(0);
    final BasicDataSource dataSource = new BasicDataSource();
    if (jdbc._driverClassName() == null || jdbc._driverClassName().size() == 0 || jdbc._driverClassName(0).text() == null)
      throw new SQLException("jdbc.driverClassName is not present");

    dataSource.setDriverClassName(jdbc._driverClassName(0).text());

//      if(jdbc._loginTimeout() != null && jdbc._loginTimeout().size() != 0 && jdbc._loginTimeout(0).text() != null)
//      {
// FIXME: This causes a ClassNotFoundException: com.sybase.jdbc3.jdbc.SybDriver
//          try
//          {
//              dataSource.setLoginTimeout(jdbc._loginTimeout(0).text());
//          }
//          catch(final SQLException e)
//          {
//              throw new SQLException(e);
//          }
//      }

    if (jdbc._url() == null || jdbc._url().size() == 0 || jdbc._url(0).text() == null)
      throw new SQLException("jdbc.url is not present");

    dataSource.setUrl(jdbc._url(0).text());

    if (jdbc._username() == null || jdbc._username().size() == 0 || jdbc._username(0).text() == null)
      throw new SQLException("jdbc.username is not present");

    dataSource.setUsername(jdbc._username(0).text());

    if (jdbc._password() == null || jdbc._password().size() == 0 || jdbc._password(0).text() == null)
      throw new SQLException("jdbc.password is not present");

    dataSource.setPassword(jdbc._password(0).text());

    final $dbcp_dbcpType._default defaults = dbcpType._default(0);
    if (defaults._connectionProperties() != null && defaults._connectionProperties().size() != 0 && defaults._connectionProperties(0)._property() != null && defaults._connectionProperties(0)._property().size() != 0)
      for (final $dbcp_dbcpType._default._connectionProperties._property property : defaults._connectionProperties(0)._property())
        if (property._name$() != null && property._name$().text() != null && property._value$() != null && property._value$().text() != null)
          dataSource.addConnectionProperty(property._name$().text(), property._value$().text());

    if (defaults._autoCommit() != null && defaults._autoCommit().size() != 0 && defaults._autoCommit(0).text() != null)
      dataSource.setDefaultAutoCommit(defaults._autoCommit(0).text());

    if (defaults._readOnly() != null && defaults._readOnly().size() != 0 && defaults._readOnly(0).text() != null)
      dataSource.setDefaultReadOnly(defaults._readOnly(0).text());

    if (defaults._transactionIsolation() != null && defaults._transactionIsolation().size() != 0 && defaults._transactionIsolation(0).text() != null) {
      if ($dbcp_dbcpType._default._transactionIsolation.NONE.text().equals(defaults._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
      else if ($dbcp_dbcpType._default._transactionIsolation.READ_5FCOMMITTED.text().equals(defaults._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      else if ($dbcp_dbcpType._default._transactionIsolation.READ_5FUNCOMMITTED.text().equals(defaults._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
      else if ($dbcp_dbcpType._default._transactionIsolation.REPEATABLE_5FREAD.text().equals(defaults._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
      else if ($dbcp_dbcpType._default._transactionIsolation.SERIALIZABLE.text().equals(defaults._transactionIsolation(0).text()))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }

    if (dbcpType._size() != null && dbcpType._size().size() != 0) {
      final $dbcp_dbcpType._size size = dbcpType._size(0);

      if (size._initialSize() != null && size._initialSize().size() != 0 && size._initialSize(0).text() != null)
        dataSource.setInitialSize(size._initialSize(0).text());

      if (size._maxActive() != null && size._maxActive().size() != 0 && size._maxActive(0).text() != null)
        dataSource.setMaxActive(size._maxActive(0).text());

      if (size._maxIdle() != null && size._maxIdle().size() != 0 && size._maxIdle(0).text() != null)
        dataSource.setMaxIdle(size._maxIdle(0).text());

      if (size._minIdle() != null && size._minIdle().size() != 0 && size._minIdle(0).text() != null)
        dataSource.setMinIdle(size._minIdle(0).text());

      if (size._maxWait() != null && size._maxWait().size() != 0 && size._maxWait(0).text() != null)
        dataSource.setMaxWait(size._maxWait(0).text());
    }

    if (dbcpType._management() != null && dbcpType._management().size() != 0) {
      final $dbcp_dbcpType._management management = dbcpType._management(0);

      if (management._validationQuery() != null && management._validationQuery().size() != 0 && management._validationQuery(0).text() != null)
        dataSource.setValidationQuery(management._validationQuery(0).text());

      if (management._testOnBorrow() != null && management._testOnBorrow().size() != 0 && management._testOnBorrow(0).text() != null)
        dataSource.setTestOnBorrow(management._testOnBorrow(0).text());

      if (management._testOnReturn() != null && management._testOnReturn().size() != 0 && management._testOnReturn(0).text() != null)
        dataSource.setTestOnReturn(management._testOnReturn(0).text());

      if (management._testWhileIdle() != null && management._testWhileIdle().size() != 0 && management._testWhileIdle(0).text() != null)
        dataSource.setTestWhileIdle(management._testWhileIdle(0).text());

      if (management._timeBetweenEvictionRuns() != null && management._timeBetweenEvictionRuns().size() != 0 && management._timeBetweenEvictionRuns(0).text() != null)
        dataSource.setTimeBetweenEvictionRunsMillis(management._timeBetweenEvictionRuns(0).text());

      if (management._numTestsPerEvictionRun() != null && management._numTestsPerEvictionRun().size() != 0 && management._numTestsPerEvictionRun(0).text() != null)
        dataSource.setNumTestsPerEvictionRun(management._numTestsPerEvictionRun(0).text());

      if (management._minEvictableIdleTime() != null && management._minEvictableIdleTime().size() != 0 && management._minEvictableIdleTime(0).text() != null)
        dataSource.setMinEvictableIdleTimeMillis(management._minEvictableIdleTime(0).text());
    }

    if (dbcpType._preparedStatements() != null && dbcpType._preparedStatements().size() != 0) {
      final $dbcp_dbcpType._preparedStatements preparedStatements = dbcpType._preparedStatements(0);

      if (preparedStatements._poolPreparedStatements() != null && preparedStatements._poolPreparedStatements().size() != 0 && preparedStatements._poolPreparedStatements(0).text() != null)
        dataSource.setPoolPreparedStatements(preparedStatements._poolPreparedStatements(0).text());

      if (preparedStatements._maxOpenPreparedStatements() != null && preparedStatements._maxOpenPreparedStatements().size() != 0 && preparedStatements._maxOpenPreparedStatements(0).text() != null)
        dataSource.setMaxOpenPreparedStatements(preparedStatements._maxOpenPreparedStatements(0).text());
    }

    if (dbcpType._removal() != null && dbcpType._removal().size() != 0) {
      final $dbcp_dbcpType._removal removal = dbcpType._removal(0);

      if (removal._removeAbandoned() != null && removal._removeAbandoned().size() != 0 && removal._removeAbandoned(0).text() != null)
        dataSource.setRemoveAbandoned(removal._removeAbandoned(0).text());

      if (removal._removeAbandonedTimeout() != null && removal._removeAbandonedTimeout().size() != 0 && removal._removeAbandonedTimeout(0).text() != null)
        dataSource.setRemoveAbandonedTimeout(removal._removeAbandonedTimeout(0).text());

      if (removal._logAbandoned() != null && removal._logAbandoned().size() != 0 && removal._logAbandoned(0).text() != null)
        dataSource.setLogAbandoned(removal._logAbandoned(0).text());
    }

    return dataSource;
  }

  private DataSources() {
  }
}