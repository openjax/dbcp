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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.jaxsb.runtime.Bindings;
import org.junit.Test;
import org.openjax.www.dbcp_1_0_4.xL0gluGCXYYJc.$Dbcp;

public class DataSourcesTest {
  @Test
  public void testJaxb() throws Exception {
    final DataSource dataSource = DataSources.createDataSource(ClassLoader.getSystemClassLoader().getResource("dbcp.xml"));
    try (final Connection connection = dataSource.getConnection()) {
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
  public void testXsb() throws Exception {
    final $Dbcp dbcp = ($Dbcp)Bindings.parse(ClassLoader.getSystemClassLoader().getResource("dbcp.xml"));
    final DataSource dataSource = DataSources.createDataSource(dbcp);
    try (final Connection connection = dataSource.getConnection()) {
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