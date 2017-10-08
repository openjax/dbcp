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

package org.lib4j.dbcp;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.lib4j.dbcp.DataSources;

public class DataSourcesTest {
  @Test
  public void test() throws Exception {
    final DataSource dataSource = DataSources.createDataSource(Thread.currentThread().getContextClassLoader().getResource("dbcp.xml"));
    try (final Connection connection = dataSource.getConnection()) {
      if (connection != null) {
        try (
          final Statement statement = connection.createStatement();
          final ResultSet resultSet = statement.executeQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
        ) {
          Assert.assertTrue(resultSet.next());
          Assert.assertEquals(1, resultSet.getInt(1));
          Assert.assertFalse(resultSet.next());
        }
      }
    }

    new File("derby.log").delete();
  }
}