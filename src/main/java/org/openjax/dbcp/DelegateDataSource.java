/* Copyright (c) 2021 OpenJAX
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

import static org.libj.lang.Assertions.*;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * A {@link DelegateDataSource} contains some other {@link DataSource}, possibly
 * transforming the method parameters along the way or providing additional
 * functionality. The class {@link DelegateDataSource} itself simply overrides
 * all methods of {@link DataSource} with versions that delegate all calls to
 * the source {@link DataSource}. Subclasses of {@link DelegateDataSource} may
 * further override some of these methods and may also provide additional
 * methods and fields.
 */
public class DelegateDataSource implements DataSource {
  /** The target {@link DataSource}. */
  protected DataSource target;

  /**
   * Creates a new {@link DelegateDataSource} with the specified target
   * {@link DataSource}.
   *
   * @param target The target {@link DataSource}.
   * @throws IllegalArgumentException If {@code target} is null.
   */
  public DelegateDataSource(final DataSource target) {
    this.target = assertNotNull(target);
  }

  /**
   * Creates a new {@link DelegateDataSource} with a null target.
   */
  protected DelegateDataSource() {
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return target.getLogWriter();
  }

  @Override
  public void setLogWriter(final PrintWriter out) throws SQLException {
    target.setLogWriter(out);
  }

  @Override
  public void setLoginTimeout(final int seconds) throws SQLException {
    target.setLoginTimeout(seconds);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return target.getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return target.getParentLogger();
  }

  @Override
  public <T> T unwrap(final Class<T> iface) throws SQLException {
    return target.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(final Class<?> iface) throws SQLException {
    return target.isWrapperFor(iface);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return target.getConnection();
  }

  @Override
  public Connection getConnection(final String username, final String password) throws SQLException {
    return target.getConnection(username, password);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this)
      return true;

    if (!(obj instanceof DelegateDataSource))
      return false;

    final DelegateDataSource that = (DelegateDataSource)obj;
    return Objects.equals(target, that.target);
  }

  @Override
  public int hashCode() {
    int hashCode = 1;
    if (target != null)
      hashCode = 31 * hashCode + target.hashCode();

    return hashCode;
  }

  @Override
  public String toString() {
    return String.valueOf(target);
  }
}