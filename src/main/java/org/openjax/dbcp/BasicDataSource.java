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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTransientConnectionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.libj.lang.Throwables;
import org.libj.sql.exception.SQLExceptions;

/**
 * An extension of {@link org.apache.commons.dbcp2.BasicDataSource} that does
 * not initialize the data source upon first invocation of
 * {@link #setLogWriter(PrintWriter)} or {@link #getLogWriter()}.
 */
class BasicDataSource extends org.apache.commons.dbcp2.BasicDataSource {
  private final AtomicBoolean initialized = new AtomicBoolean(false);
  private final AtomicBoolean settingLogWriter = new AtomicBoolean(false);
  private PrintWriter logWriter;

  @Override
  public Connection getConnection() throws SQLException {
    try {
      if (!initialized.get()) {
        synchronized (this) {
          if (!initialized.get()) {
            this.initialized.set(true);
            if (logWriter != null)
              setLogWriter(logWriter);
          }
        }
      }

      return super.getConnection();
    }
    catch (final SQLException e) {
      if (e.getMessage() == null || !e.getMessage().startsWith("Cannot get a connection"))
        throw SQLExceptions.toStrongType(e);

      final Throwable cause = e.getCause();
      if (cause.getMessage() != null && cause.getMessage().startsWith("Timeout waiting"))
        throw Throwables.copy(e, new SQLTransientConnectionException(e.getMessage(), e.getSQLState(), e.getErrorCode()));

      throw Throwables.copy(e, new SQLNonTransientConnectionException(e.getMessage(), e.getSQLState(), e.getErrorCode()));
    }
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return initialized.get() ? super.getLogWriter() : logWriter;
  }

  @Override
  protected DataSource createDataSource() throws SQLException {
    if (settingLogWriter.get()) {
      synchronized (this) {
        if (settingLogWriter.get()) {
          return this;
        }
      }
    }

    return super.createDataSource();
  }

  @Override
  public void setLogWriter(final PrintWriter logWriter) throws SQLException {
    if (settingLogWriter.get())
      return;

    if (initialized.get()) {
      synchronized (this) {
        settingLogWriter.set(true);
        super.setLogWriter(logWriter);
        settingLogWriter.set(false);
        super.createDataSource();
      }
    }
    else {
      this.logWriter = logWriter;
    }
  }
}