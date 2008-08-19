package org.safris.commons.dbcp;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;
import org.safris.xml.schema.binding.dbcp.$dbcp_dbcpType;

public final class DataSources {
    public static BasicDataSource createDataSource($dbcp_dbcpType<?> dbcpType) throws SQLException {
        if (dbcpType.get_jdbc() == null || dbcpType.get_jdbc().size() == 0)
            throw new SQLException("jdbc is not present");

        final $dbcp_dbcpType._jdbc jdbc = dbcpType.get_jdbc().get(0);
        final BasicDataSource dataSource = new BasicDataSource();
        if (jdbc.get_driverClassName() == null || jdbc.get_driverClassName().size() == 0 || jdbc.get_driverClassName().get(0).getText() == null)
            throw new SQLException("jdbc.driverClassName is not present");

        dataSource.setDriverClassName(jdbc.get_driverClassName().get(0).getText());

        if (jdbc.get_loginTimeout() != null && jdbc.get_loginTimeout().size() != 0 && jdbc.get_loginTimeout().get(0).getText() != null) {
            // FIXME: This causes a ClassNotFoundException: com.sybase.jdbc3.jdbc.SybDriver
//            try {
//                dataSource.setLoginTimeout(jdbc.get_loginTimeout().get(0).getText());
//            }
//            catch (SQLException e) {
//                throw new SQLException(e);
//            }
        }

        if (jdbc.get_url() == null || jdbc.get_url().size() == 0 || jdbc.get_url().get(0).getText() == null)
            throw new SQLException("jdbc.url is not present");

        dataSource.setUrl(jdbc.get_url().get(0).getText());

        if (jdbc.get_username() == null || jdbc.get_username().size() == 0 || jdbc.get_username().get(0).getText() == null)
            throw new SQLException("jdbc.username is not present");

        dataSource.setUsername(jdbc.get_username().get(0).getText());

        if (jdbc.get_password() == null || jdbc.get_password().size() == 0 || jdbc.get_password().get(0).getText() == null)
            throw new SQLException("jdbc.password is not present");

        dataSource.setPassword(jdbc.get_password().get(0).getText());

        final $dbcp_dbcpType._default defaults = dbcpType.get_default().get(0);
        if (defaults.get_connectionProperties() != null && defaults.get_connectionProperties().size() != 0 && defaults.get_connectionProperties().get(0).get_property() != null && defaults.get_connectionProperties().get(0).get_property().size() != 0)
            for ($dbcp_dbcpType._default._connectionProperties._property property : defaults.get_connectionProperties().get(0).get_property())
                if (property.get_name$() != null && property.get_name$().getText() != null && property.get_value$() != null && property.get_value$().getText() != null)
                    dataSource.addConnectionProperty(property.get_name$().getText(), property.get_value$().getText());

        if (defaults.get_autoCommit() != null && defaults.get_autoCommit().size() != 0 && defaults.get_autoCommit().get(0).getText() != null)
            dataSource.setDefaultAutoCommit(defaults.get_autoCommit().get(0).getText());

        if (defaults.get_readOnly() != null && defaults.get_readOnly().size() != 0 && defaults.get_readOnly().get(0).getText() != null)
            dataSource.setDefaultReadOnly(defaults.get_readOnly().get(0).getText());

        if (defaults.get_transactionIsolation() != null && defaults.get_transactionIsolation().size() != 0 && defaults.get_transactionIsolation().get(0).getText() != null) {
            if ($dbcp_dbcpType._default._transactionIsolation.NONE.getText().equals(defaults.get_transactionIsolation().get(0).getText()))
                dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
            else if ($dbcp_dbcpType._default._transactionIsolation.READ_COMMITTED.getText().equals(defaults.get_transactionIsolation().get(0).getText()))
                dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            else if ($dbcp_dbcpType._default._transactionIsolation.READ_UNCOMMITTED.getText().equals(defaults.get_transactionIsolation().get(0).getText()))
                dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            else if ($dbcp_dbcpType._default._transactionIsolation.REPEATABLE_READ.getText().equals(defaults.get_transactionIsolation().get(0).getText()))
                dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            else if ($dbcp_dbcpType._default._transactionIsolation.SERIALIZABLE.getText().equals(defaults.get_transactionIsolation().get(0).getText()))
                dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        }

        if (dbcpType.get_size() != null && dbcpType.get_size().size() != 0) {
            final $dbcp_dbcpType._size size = dbcpType.get_size().get(0);

            if (size.get_initialSize() != null && size.get_initialSize().size() != 0 && size.get_initialSize().get(0).getText() != null)
                dataSource.setInitialSize(size.get_initialSize().get(0).getText());

            if (size.get_maxActive() != null && size.get_maxActive().size() != 0 && size.get_maxActive().get(0).getText() != null)
                dataSource.setMaxActive(size.get_maxActive().get(0).getText());

            if (size.get_maxIdle() != null && size.get_maxIdle().size() != 0 && size.get_maxIdle().get(0).getText() != null)
                dataSource.setMaxIdle(size.get_maxIdle().get(0).getText());

            if (size.get_minIdle() != null && size.get_minIdle().size() != 0 && size.get_minIdle().get(0).getText() != null)
                dataSource.setMinIdle(size.get_minIdle().get(0).getText());

            if (size.get_maxWait() != null && size.get_maxWait().size() != 0 && size.get_maxWait().get(0).getText() != null)
                dataSource.setMaxWait(size.get_maxWait().get(0).getText());
        }

        if (dbcpType.get_management() != null && dbcpType.get_management().size() != 0) {
            final $dbcp_dbcpType._management management = dbcpType.get_management().get(0);

            if (management.get_validationQuery() != null && management.get_validationQuery().size() != 0 && management.get_validationQuery().get(0).getText() != null)
                dataSource.setValidationQuery(management.get_validationQuery().get(0).getText());

            if (management.get_testOnBorrow() != null && management.get_testOnBorrow().size() != 0 && management.get_testOnBorrow().get(0).getText() != null)
                dataSource.setTestOnBorrow(management.get_testOnBorrow().get(0).getText());

            if (management.get_testOnReturn() != null && management.get_testOnReturn().size() != 0 && management.get_testOnReturn().get(0).getText() != null)
                dataSource.setTestOnReturn(management.get_testOnReturn().get(0).getText());

            if (management.get_testWhileIdle() != null && management.get_testWhileIdle().size() != 0 && management.get_testWhileIdle().get(0).getText() != null)
                dataSource.setTestWhileIdle(management.get_testWhileIdle().get(0).getText());

            if (management.get_timeBetweenEvictionRuns() != null && management.get_timeBetweenEvictionRuns().size() != 0 && management.get_timeBetweenEvictionRuns().get(0).getText() != null)
                dataSource.setTimeBetweenEvictionRunsMillis(management.get_timeBetweenEvictionRuns().get(0).getText());

            if (management.get_numTestsPerEvictionRun() != null && management.get_numTestsPerEvictionRun().size() != 0 && management.get_numTestsPerEvictionRun().get(0).getText() != null)
                dataSource.setNumTestsPerEvictionRun(management.get_numTestsPerEvictionRun().get(0).getText());

            if (management.get_minEvictableIdleTime() != null && management.get_minEvictableIdleTime().size() != 0 && management.get_minEvictableIdleTime().get(0).getText() != null)
                dataSource.setMinEvictableIdleTimeMillis(management.get_minEvictableIdleTime().get(0).getText());
        }

        if (dbcpType.get_preparedStatements() != null && dbcpType.get_preparedStatements().size() != 0) {
            final $dbcp_dbcpType._preparedStatements preparedStatements = dbcpType.get_preparedStatements().get(0);

            if (preparedStatements.get_poolPreparedStatements() != null && preparedStatements.get_poolPreparedStatements().size() != 0 && preparedStatements.get_poolPreparedStatements().get(0).getText() != null)
                dataSource.setPoolPreparedStatements(preparedStatements.get_poolPreparedStatements().get(0).getText());

            if (preparedStatements.get_maxOpenPreparedStatements() != null && preparedStatements.get_maxOpenPreparedStatements().size() != 0 && preparedStatements.get_maxOpenPreparedStatements().get(0).getText() != null)
                dataSource.setMaxOpenPreparedStatements(preparedStatements.get_maxOpenPreparedStatements().get(0).getText());
        }

        if (dbcpType.get_removal() != null && dbcpType.get_removal().size() != 0) {
            final $dbcp_dbcpType._removal removal = dbcpType.get_removal().get(0);

            if (removal.get_removeAbandoned() != null && removal.get_removeAbandoned().size() != 0 && removal.get_removeAbandoned().get(0).getText() != null)
                dataSource.setRemoveAbandoned(removal.get_removeAbandoned().get(0).getText());

            if (removal.get_removeAbandonedTimeout() != null && removal.get_removeAbandonedTimeout().size() != 0 && removal.get_removeAbandonedTimeout().get(0).getText() != null)
                dataSource.setRemoveAbandonedTimeout(removal.get_removeAbandonedTimeout().get(0).getText());

            if (removal.get_logAbandoned() != null && removal.get_logAbandoned().size() != 0 && removal.get_logAbandoned().get(0).getText() != null)
                dataSource.setLogAbandoned(removal.get_logAbandoned().get(0).getText());
        }

        return dataSource;
    }

    private DataSources() {
    }
}
