package org.safris.commons.dbcp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Ignore;
import org.junit.Test;
import org.safris.commons.lang.Resources;
import org.safris.xml.generator.compiler.runtime.Bindings;
import org.xml.sax.InputSource;

public final class DataSourcesTest {
  @Test
  @Ignore("Need to have an embedded DB to test against.")
  public void testJNDIDataSource() throws Exception {
    final dbcp_dbcp dbcp = (dbcp_dbcp)Bindings.parse(new InputSource(Resources.getResource("dbcp.xml").getURL().openStream()));
    final DataSource dataSource = DataSources.createDataSource(dbcp);
    final Connection connection = dataSource.getConnection();
    if (connection != null) {
      final Statement statement = connection.createStatement();
      final ResultSet resultSet = statement.executeQuery("SELECT 1");
      while (resultSet.next()) {
        final String string = resultSet.getString(1);
        System.out.println("C : " + string);
      }

      resultSet.close();
      statement.close();
      connection.close();
    }
  }
}