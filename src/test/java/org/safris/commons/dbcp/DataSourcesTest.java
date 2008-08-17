package org.safris.commons.dbcp;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.safris.xml.generator.compiler.runtime.Bindings;
import org.safris.xml.schema.binding.dbcp.$dbcp_dbcpType;
import org.xml.sax.InputSource;

import static org.junit.Assert.*;

public class DataSourcesTest
{
	public static void main(String[] args) throws Exception
	{
		final DataSourcesTest dataSourcesTest = new DataSourcesTest();
		dataSourcesTest.testJNDIDataSource();
	}

	@Test
	@Ignore("Need to have an in-memory DB to test against.")
	public void testJNDIDataSource() throws Exception
	{
		final $dbcp_dbcpType<?> dbcpType = ($dbcp_dbcpType<?>)Bindings.parse(new InputSource(new FileInputStream(new File("src/test/resources/xml/dbcp.xml"))));
		final DataSource dataSource = DataSources.createDataSource(dbcpType);
        final Connection connection = dataSource.getConnection();
        if(connection != null)
		{
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery("SELECT 1");
            while(resultSet.next())
			{
                final String string = resultSet.getString(1);
                System.out.println("C : " + string);
            }

            resultSet.close();
            statement.close();
            connection.close();
        }
	}
}
