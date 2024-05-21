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

import static org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.libj.lang.Strings;
import org.libj.logging.LoggerPrintWriter;
import org.libj.util.CollectionUtil;
import org.openjax.dbcp_1_2.Dbcp;
import org.openjax.dbcp_1_2.Dbcp.Pool.RemoveAbandoned;
import org.openjax.www.dbcp_1_2.xL0gluGCXAA.$Dbcp;
import org.openjax.www.dbcp_1_2.xL0gluGCXAA.$Dbcps;
import org.openjax.www.dbcp_1_2.xL0gluGCXAA.$IndefinitePositiveInt;
import org.openjax.www.dbcp_1_2.xL0gluGCXAA.$IndefinitePositiveLong;
import org.openjax.www.dbcp_1_2.xL0gluGCXAA.Dbcps;
import org.openjax.www.xml.datatypes_0_9.xL9gluGCXAA.$NonNegativeInt;
import org.openjax.www.xml.datatypes_0_9.xL9gluGCXAA.$NonNegativeLong;
import org.openjax.www.xml.datatypes_0_9.xL9gluGCXAA.$PositiveInt;
import org.openjax.www.xml.datatypes_0_9.xL9gluGCXAA.$QualifiedIdentifier;
import org.openjax.www.xml.datatypes_0_9.xL9gluGCXAA.$StringNonEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.w3.www._2001.XMLSchema.yAA.$AnyURI;
import org.w3.www._2001.XMLSchema.yAA.$Boolean;
import org.xml.sax.SAXException;

public final class DataSources {
  private static final List<String> defaultDisconnectionQueryCodes = Arrays.asList("57P01", "57P02", "57P03", "01002", "JZ0C0", "JZ0C1");
  private static final String INDEFINITE = "INDEFINITE";
  private static final String schemaFile = "dbcp.xsd";
  private static Schema schema;

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the {@link URL url} specifying an xml document with root
   * element {@code dbcp:dbcp}. {@link ClassLoader#getSystemClassLoader()} will be used by the {@link BasicDataSource} when it loads
   * the JDBC driver.
   *
   * @param url An {@link URL} specifying a dbcp xml resource.
   * @return The {@link BasicDataSource} instance.
   * @throws IOException If an I/O error has occurred
   * @throws SAXException If the xml document does not have a {@code dbcp:dbcp} root element, or if an XML validation error has
   *           occurred.
   * @throws NullPointerException If {@code url} is null.
   * @throws IllegalArgumentException If the {@code /dbcp:dbcp/dbcp:jdbc} element is missing.
   */
  public static BasicDataSource createDataSource(final URL url) throws IOException, SAXException {
    return createDataSource(url, ClassLoader.getSystemClassLoader());
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the {@link URL url} specifying an xml document with root
   * element {@code dbcp:dbcp}. {@link ClassLoader#getSystemClassLoader()} will be used by the {@link BasicDataSource} when it loads
   * the JDBC driver.
   *
   * @param url An {@link URL} specifying a dbcp xml resource.
   * @param driverClassLoader Class loader to be used by the {@link BasicDataSource} when it loads the JDBC driver.
   * @return The {@link BasicDataSource} instance.
   * @throws IOException If an I/O error has occurred
   * @throws SAXException If the xml document does not have a {@code dbcp:dbcp} root element, or if an XML validation error has
   *           occurred.
   * @throws NullPointerException If {@code url} is null.
   * @throws IllegalArgumentException If the {@code /dbcp:dbcp/dbcp:jdbc} element is missing.
   */
  public static BasicDataSource createDataSource(final URL url, final ClassLoader driverClassLoader) throws IOException, SAXException {
    try {
      final Unmarshaller unmarshaller = JAXBContext.newInstance(Dbcp.class).createUnmarshaller();
      final URL resource = Thread.currentThread().getContextClassLoader().getResource(schemaFile);
      if (resource == null)
        throw new IllegalStateException("Unable to find " + schemaFile + " in class loader " + Thread.currentThread().getContextClassLoader());

      unmarshaller.setSchema(DataSources.schema == null ? DataSources.schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(resource) : DataSources.schema);
      try (final InputStream in = url.openStream()) {
        final JAXBElement<Dbcp> element = unmarshaller.unmarshal(XMLInputFactory.newInstance().createXMLStreamReader(in), Dbcp.class);
        return createDataSource(driverClassLoader, element.getValue());
      }
    }
    catch (final FactoryConfigurationError e) {
      throw new UnsupportedOperationException(e);
    }
    catch (final JAXBException | XMLStreamException e) {
      throw new SAXException(e);
    }
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the array of {@link Dbcp dbcp} JAX-B bindings that match the
   * specified {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the {@link BasicDataSource} when it loads the
   * JDBC driver.
   *
   * @param id The id of the {@link Dbcp dbcp} bindings to match, or {@code null} to match all bindings.
   * @param dbcps Array of {@link Dbcp} JAX-B bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If the {@code /dbcp:jdbc} element is missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final Dbcp ... dbcps) {
    return createDataSource(id, ClassLoader.getSystemClassLoader(), dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the array of {@link Dbcp dbcp} JAX-B bindings that match any
   * {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param dbcps Array of {@link Dbcp} JAX-B bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If the {@code /dbcp:jdbc} element is missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final Dbcp ... dbcps) {
    return createDataSource(null, ClassLoader.getSystemClassLoader(), dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the array of {@link $Dbcp dbcp} JAX-SB bindings that match
   * the specified {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the {@link BasicDataSource} when it loads
   * the JDBC driver.
   *
   * @param id The id of the {@code /dbcp:dbcp} child elements of the provided {@link $Dbcp} to match, or {@code null} to match all
   *          child elements.
   * @param dbcps Array of {@link $Dbcp} JAX-SB bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If the {@code /dbcp:jdbc} element is missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final $Dbcp ... dbcps) {
    return createDataSource(id, ClassLoader.getSystemClassLoader(), dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the array of {@link $Dbcp dbcp} JAX-SB bindings that match
   * any {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the {@link BasicDataSource} when it loads the JDBC
   * driver.
   *
   * @param dbcps Array of {@link $Dbcp} JAX-SB bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If the {@code /dbcp:jdbc} element is missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final $Dbcp ... dbcps) {
    return createDataSource(null, ClassLoader.getSystemClassLoader(), dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the {@code /dbcp:dbcp} child elements of the provided
   * {@link org.openjax.dbcp_1_2.Dbcps} JAX-B binding that match the specified {@code id}. {@link ClassLoader#getSystemClassLoader()}
   * will be used by the {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param id The id of the {@code /dbcp:dbcp} child elements of the provided {@link org.openjax.dbcp_1_2.Dbcps} to match, or
   *          {@code null} to match all child elements.
   * @param dbcps The {@link org.openjax.dbcp_1_2.Dbcps} JAX-B binding.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, or any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If {@code dbcps} does not contain any {@code /dbcp:dbcp} child elements, or if the
   *           {@code /dbcp:jdbc} element is missing from all {@code /dbcp:dbcp} child elements in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final org.openjax.dbcp_1_2.Dbcps dbcps) {
    return createDataSource(id, ClassLoader.getSystemClassLoader(), dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the {@code /dbcp:dbcp} child elements of the provided
   * {@link org.openjax.dbcp_1_2.Dbcps} JAX-B binding that match any {@code id}. {@link ClassLoader#getSystemClassLoader()} will be
   * used by the {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param dbcps The {@link org.openjax.dbcp_1_2.Dbcps} JAX-B binding.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, or any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If {@code dbcps} does not contain any {@code /dbcp:dbcp} child elements, or if the
   *           {@code /dbcp:jdbc} element is missing from all {@code /dbcp:dbcp} child elements in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final org.openjax.dbcp_1_2.Dbcps dbcps) {
    return createDataSource(null, ClassLoader.getSystemClassLoader(), dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the {@code /dbcp:dbcp} child elements of the provided
   * {@link Dbcps} JAX-SB binding that match the specified {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the
   * {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param id The id of the {@code /dbcp:dbcp} child elements of the provided {@link Dbcps} to match, or {@code null} to match all
   *          child elements.
   * @param dbcps The {@link Dbcps} JAX-SB binding.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, or any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If {@code dbcps} does not contain any {@code /dbcp:dbcp} child elements, or if the
   *           {@code /dbcp:jdbc} element is missing from all {@code /dbcp:dbcp} child elements in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final $Dbcps dbcps) {
    return createDataSource(id, ClassLoader.getSystemClassLoader(), dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the {@code /dbcp:dbcp} child elements of the provided
   * {@link Dbcps} JAX-SB binding that match any {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the
   * {@link BasicDataSource} when it loads the JDBC driver.
   *
   * @param dbcps The {@link Dbcps} JAX-SB binding.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, or any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If {@code dbcps} does not contain any {@code /dbcp:dbcp} child elements, or if the
   *           {@code /dbcp:jdbc} element is missing from all {@code /dbcp:dbcp} child elements in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final $Dbcps dbcps) {
    return createDataSource(null, ClassLoader.getSystemClassLoader(), dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the {@code /dbcp:dbcp} child elements of the provided
   * {@link Dbcps} JAX-B binding that match any {@code id}.
   *
   * @param driverClassLoader Class loader to be used by the {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps Array of {@link Dbcp} JAX-B bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, or any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If the {@code /dbcp:jdbc} element is missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final ClassLoader driverClassLoader, final Dbcp ... dbcps) {
    return createDataSource(null, driverClassLoader, dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the array of {@link $Dbcp dbcp} JAX-SB bindings that match
   * any {@code id}.
   *
   * @param driverClassLoader Class loader to be used by the {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps Array of {@link $Dbcp} JAX-SB bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, or any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If the {@code /dbcp:jdbc} element is missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final ClassLoader driverClassLoader, final $Dbcp ... dbcps) {
    return createDataSource(null, driverClassLoader, dbcps);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the {@code /dbcp:dbcp} child elements of the provided
   * {@link org.openjax.dbcp_1_2.Dbcps} JAX-B binding that match the specified {@code id}.
   *
   * @param id The id of the {@code /dbcp:dbcp} child elements of the provided {@link org.openjax.dbcp_1_2.Dbcps} to match, or
   *          {@code null} to match all child elements.
   * @param driverClassLoader Class loader to be used by the {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps The {@link org.openjax.dbcp_1_2.Dbcps} JAX-B binding.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, or any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If {@code dbcps} does not contain any {@code /dbcp:dbcp} child elements, or if the
   *           {@code /dbcp:jdbc} element is missing from all {@code /dbcp:dbcp} child elements in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final ClassLoader driverClassLoader, final org.openjax.dbcp_1_2.Dbcps dbcps) {
    return createDataSource(id, driverClassLoader, dbcps.getDbcp().toArray(new $Dbcp[dbcps.getDbcp().size()]));
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the {@code /dbcp:dbcp} child elements of the provided
   * {@link org.openjax.dbcp_1_2.Dbcps} JAX-B binding that match any {@code id}.
   *
   * @param driverClassLoader Class loader to be used by the {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps The {@link org.openjax.dbcp_1_2.Dbcps} JAX-B binding.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, or any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If {@code dbcps} does not contain any {@code /dbcp:dbcp} child elements, or if the
   *           {@code /dbcp:jdbc} element is missing from all {@code /dbcp:dbcp} child elements in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final ClassLoader driverClassLoader, final org.openjax.dbcp_1_2.Dbcps dbcps) {
    return createDataSource(null, driverClassLoader, dbcps.getDbcp().toArray(new $Dbcp[dbcps.getDbcp().size()]));
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the {@code /dbcp:dbcp} child elements of the provided
   * {@link Dbcps} JAX-SB binding that match the specified {@code id}.
   *
   * @param id The id of the {@code /dbcp:dbcp} child elements of the provided {@link Dbcps} to match, or {@code null} to match all
   *          child elements.
   * @param driverClassLoader Class loader to be used by the {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps The {@link Dbcps} JAX-SB binding.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, or any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If {@code dbcps} does not contain any {@code /dbcp:dbcp} child elements, or if the
   *           {@code /dbcp:jdbc} element is missing from all {@code /dbcp:dbcp} child elements in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final ClassLoader driverClassLoader, final $Dbcps dbcps) {
    return createDataSource(id, driverClassLoader, dbcps.getDbcpDbcp().toArray(new $Dbcp[dbcps.getDbcpDbcp().size()]));
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the {@code /dbcp:dbcp} child elements of the provided
   * {@link Dbcps} JAX-SB binding that match any {@code id}.
   *
   * @param driverClassLoader Class loader to be used by the {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps The {@link Dbcps} JAX-SB binding.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, or any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If {@code dbcps} does not contain any {@code /dbcp:dbcp} child elements, or if the
   *           {@code /dbcp:jdbc} element is missing from all {@code /dbcp:dbcp} child elements in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final ClassLoader driverClassLoader, final $Dbcps dbcps) {
    return createDataSource(null, driverClassLoader, dbcps.getDbcpDbcp().toArray(new $Dbcp[dbcps.getDbcpDbcp().size()]));
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the array of {@link Dbcp dbcp} JAX-B bindings that that match
   * any {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the {@link BasicDataSource} when it loads the JDBC
   * driver.
   *
   * @param dbcp The {@link Dbcp} JAX-B bindings providing the configuration.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcp} is null.
   * @throws IllegalArgumentException If the {@code /dbcp:jdbc} element is missing.
   */
  public static BasicDataSource createDataSource(final Dbcp dbcp) {
    return createDataSource(null, ClassLoader.getSystemClassLoader(), dbcp);
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the array of {@link $Dbcp dbcp} JAX-SB bindings that match
   * any {@code id}. {@link ClassLoader#getSystemClassLoader()} will be used by the {@link BasicDataSource} when it loads the JDBC
   * driver.
   *
   * @param dbcp The {@link $Dbcp} JAX-SB bindings providing the configuration.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcp} is null.
   * @throws IllegalArgumentException If the {@code /dbcp:jdbc} element is missing.
   */
  public static BasicDataSource createDataSource(final $Dbcp dbcp) {
    return createDataSource(null, ClassLoader.getSystemClassLoader(), dbcp);
  }

  private final ClassLoader driverClassLoader;
  private BasicDataSource dataSource = null;
  private String driverClassName = null;
  private String url = null;

  private boolean autoCommit = true;
  private boolean readOnly = false;

  private Integer queryTimeout = null;
  private String transactionIsolation = null;

  private int initialSize = 0;
  private int minIdle = 0;
  private String maxIdle = INDEFINITE;
  private String maxTotal = INDEFINITE;
  private boolean poolPreparedStatements = false;
  private String maxOpen = INDEFINITE;

  private boolean lifo = true;

  private boolean cacheState = true;
  private String maxWait = INDEFINITE;
  private String maxConnLifetime = INDEFINITE;
  private boolean autoCommitOnReturn = true;
  private boolean rollbackOnReturn = true;
  private String removeAbandonedOn = null;
  private int removeAbandonedTimeout = 0;
  private boolean abandonedUsageTracking = false;
  private boolean accessToUnderlyingConnectionAllowed = false;
  private boolean hasEviction = false;
  private String timeBetweenEvictionRunsMillis = INDEFINITE;
  private int numTestsPerRun = 3;
  private long minEvictableIdleTimeMillis = 1800000;
  private String softMinEvictableIdleTimeMillis = INDEFINITE;
  private String policyClassName = null;

  private boolean hasValidation = false;
  private String validationQuery = null;
  private String validationQueryTimeout = INDEFINITE;
  private boolean testOnCreate = false;
  private boolean testOnBorrow = true;
  private boolean testOnReturn = false;
  private boolean testWhileIdle = false;
  private List<String> disconnectionQueryCodes = null;

  private String loggingLevel = null;
  private boolean logExpiredConnections = false;
  private boolean logAbandoned = false;

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the array of {@link Dbcp dbcp} JAX-B bindings that match the
   * specified {@code id}.
   *
   * @param id The id of the {@link Dbcp dbcp} bindings to match, or {@code null} to match all bindings.
   * @param driverClassLoader Class loader to be used by the {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps Array of {@link Dbcp} JAX-B bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If the {@code /dbcp:jdbc} element is missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final ClassLoader driverClassLoader, final Dbcp ... dbcps) {
    return new DataSources(id, driverClassLoader, dbcps).build();
  }

  /**
   * Create a {@link BasicDataSource} from the configuration supplied by the array of {@link $Dbcp dbcp} JAX-SB bindings that match
   * the specified {@code id}.
   *
   * @param id The id of the {@link Dbcp dbcp} bindings to match, or {@code null} to match all bindings.
   * @param driverClassLoader Class loader to be used by the {@link BasicDataSource} when it loads the JDBC driver.
   * @param dbcps Array of {@link $Dbcp} JAX-SB bindings.
   * @return The {@link BasicDataSource} instance.
   * @throws NullPointerException If {@code dbcps} is null, any member of {@code dbcps} is null.
   * @throws IllegalArgumentException If the {@code /dbcp:jdbc} element is missing from all members in {@code dbcps}.
   */
  public static BasicDataSource createDataSource(final String id, final ClassLoader driverClassLoader, final $Dbcp ... dbcps) {
    return new DataSources(id, driverClassLoader, dbcps).build();
  }

  private DataSources(final String id, final ClassLoader driverClassLoader, final Dbcp ... dbcps) {
    this.driverClassLoader = driverClassLoader;
    for (final Dbcp dbcp : dbcps) { // [A]
      if (id != null && !id.equals(dbcp.getId()))
        continue;

      if (dataSource == null)
        dataSource = new BasicDataSource();

      org.openjax.dbcp_1_2.Dbcp.Jdbc jdbc$ = dbcp.getJdbc();
      if (jdbc$ != null) {
        final String driverClassName$ = jdbc$.getDriverClassName();
        if (driverClassName$ != null)
          driverClassName = driverClassName$;

        final String url$ = jdbc$.getUrl();
        if (url$ != null)
          url = url$;
      }

      final Dbcp.Default _default = dbcp.getDefault();
      if (_default != null) {
        final String catalog$ = _default.getCatalog();
        if (catalog$ != null)
          dataSource.setDefaultCatalog(catalog$);

        final Boolean autoCommit$ = _default.getAutoCommit();
        if (autoCommit$ != null)
          autoCommit = autoCommit$;

        final Boolean readOnly$ = _default.getReadOnly();
        if (readOnly$ != null)
          readOnly = readOnly$;

        final Integer queryTimeout$ = _default.getQueryTimeout();
        if (queryTimeout$ != null)
          queryTimeout = queryTimeout$;

        final String transactionIsolation$ = _default.getTransactionIsolation();
        if (transactionIsolation$ != null)
          transactionIsolation = transactionIsolation$;
      }

      final Dbcp.Connection connection = dbcp.getConnection();
      if (connection != null) {
        final Dbcp.Connection.Properties properties$ = connection.getProperties();
        if (properties$ != null) {
          final List<Dbcp.Connection.Properties.Property> properties = properties$.getProperty();
          final int i$ = properties.size();
          if (i$ > 0) {
            if (CollectionUtil.isRandomAccess(properties)) {
              int i = 0;
              do // [RA]
                add(properties.get(i));
              while (++i < i$);
            }
            else {
              final Iterator<Dbcp.Connection.Properties.Property> it = properties.iterator();
              do // [I]
                add(it.next());
              while (it.hasNext());
            }
          }
        }

        final Dbcp.Connection.InitSqls initSqls$ = connection.getInitSqls();
        if (initSqls$ != null) {
          final List<String> connectionInitSqls = dataSource.getConnectionInitSqls();
          final List<String> initSql$ = initSqls$.getInitSql();
          if (connectionInitSqls.size() > 0)
            connectionInitSqls.addAll(initSql$);
          else
            dataSource.setConnectionInitSqls(initSql$);
        }
      }

      final Dbcp.Size size = dbcp.getSize();
      if (size != null) {
        final Integer initialSize$ = size.getInitialSize();
        if (initialSize$ != null)
          initialSize = initialSize$;

        final Integer minIdle$ = size.getMinIdle();
        if (minIdle$ != null)
          minIdle = minIdle$;

        final String maxIdle$ = size.getMaxIdle();
        if (maxIdle$ != null)
          maxIdle = maxIdle$;

        final String maxTotal$ = size.getMaxTotal();
        if (maxTotal$ != null)
          maxTotal = maxTotal$;

        final Dbcp.Size.PoolPreparedStatements poolPreparedStatements$ = size.getPoolPreparedStatements();
        if (poolPreparedStatements$ != null) {
          poolPreparedStatements = true;
          final String maxOpen$ = poolPreparedStatements$.getMaxOpen();
          if (maxOpen$ != null)
            maxOpen = maxOpen$;
        }
      }

      final Dbcp.Pool pool = dbcp.getPool();
      if (pool != null) {
        final String queue = pool.getQueue();
        if (queue != null) {
          if ("lifo".equals(queue))
            lifo = true;
          else if ("fifo".equals(queue))
            lifo = false;
          else
            throw new UnsupportedOperationException("Unsupported queue spec: " + queue);
        }

        final Boolean cacheState$ = pool.getCacheState();
        if (cacheState$ != null)
          cacheState = cacheState$;

        final String maxWait$ = pool.getMaxWait();
        if (maxWait$ != null)
          maxWait = maxWait$;

        final String maxConnectionLifetime$ = pool.getMaxConnectionLifetime();
        if (maxConnectionLifetime$ != null)
          maxConnLifetime = maxConnectionLifetime$;

        final Boolean autoCommitOnReturn$ = pool.getAutoCommitOnReturn();
        if (autoCommitOnReturn$ != null)
          autoCommitOnReturn = autoCommitOnReturn$;

        final Boolean rollbackOnReturn$ = pool.getRollbackOnReturn();
        if (rollbackOnReturn$ != null)
          rollbackOnReturn = rollbackOnReturn$;

        final RemoveAbandoned removeAbandoned$ = pool.getRemoveAbandoned();
        if (removeAbandoned$ != null) {
          removeAbandonedOn = removeAbandoned$.getOn();
          removeAbandonedTimeout = removeAbandoned$.getTimeout();
        }

        final Boolean abandonedUsageTracking$ = pool.getAbandonedUsageTracking();
        if (abandonedUsageTracking$ != null)
          abandonedUsageTracking = abandonedUsageTracking$;

        final Boolean allowAccessToUnderlyingConnection$ = pool.getAllowAccessToUnderlyingConnection();
        if (allowAccessToUnderlyingConnection$ != null)
          accessToUnderlyingConnectionAllowed = allowAccessToUnderlyingConnection$;

        final Dbcp.Pool.Eviction eviction = pool.getEviction();
        if (eviction != null) {
          hasEviction = true;
          final String timeBetweenRuns$ = eviction.getTimeBetweenRuns();
          if (timeBetweenRuns$ != null)
            timeBetweenEvictionRunsMillis = timeBetweenRuns$;

          final Integer numTestsPerRun$ = eviction.getNumTestsPerRun();
          if (numTestsPerRun$ != null)
            numTestsPerRun = numTestsPerRun$;

          final Long minIdleTime$ = eviction.getMinIdleTime();
          if (minIdleTime$ != null)
            minEvictableIdleTimeMillis = minIdleTime$;

          final String softMinIdleTime$ = eviction.getSoftMinIdleTime();
          if (softMinIdleTime$ != null)
            softMinEvictableIdleTimeMillis = softMinIdleTime$;

          final String policyClassName$ = eviction.getPolicyClassName();
          if (policyClassName$ != null)
            policyClassName = policyClassName$;
        }
      }

      final Dbcp.Validation validation = dbcp.getValidation();
      if (validation != null) {
        hasValidation = true;
        final String query$ = validation.getQuery();
        if (query$ != null)
          validationQuery = query$;

        final String timeout$ = validation.getTimeout();
        if (timeout$ != null)
          validationQueryTimeout = timeout$;

        final Boolean testOnCreate$ = validation.getTestOnCreate();
        if (testOnCreate$ != null)
          testOnCreate = testOnCreate$;

        final Boolean testOnBorrow$ = validation.getTestOnBorrow();
        if (testOnBorrow$ != null)
          testOnBorrow = testOnBorrow$;

        final Boolean testOnReturn$ = validation.getTestOnReturn();
        if (testOnReturn$ != null)
          testOnReturn = testOnReturn$;

        final Boolean testWhileIdle$ = validation.getTestWhileIdle();
        if (testWhileIdle$ != null)
          testWhileIdle = testWhileIdle$;

        final Dbcp.Validation.FastFail failFast = validation.getFastFail();
        if (failFast != null) {
          dataSource.setFastFailValidation(true);
          String disconnectionSqlCodes = failFast.getDisconnectionSqlCodes();
          if (disconnectionSqlCodes != null && (disconnectionSqlCodes = disconnectionSqlCodes.trim()).length() > 0) {
            if (disconnectionQueryCodes == null)
              disconnectionQueryCodes = new ArrayList<>();

            Collections.addAll(disconnectionQueryCodes, Strings.split(disconnectionSqlCodes, ' '));
          }
        }
      }

      final Dbcp.Logging logging = dbcp.getLogging();
      if (logging != null) {
        loggingLevel = logging.getLevel();
        if (logging.getLogExpiredConnections() != null)
          logExpiredConnections = logging.getLogExpiredConnections();

        if (logging.getLogAbandoned() != null)
          logAbandoned = logging.getLogAbandoned();
      }

      dataSource.setJmxName(dbcp.getJmxName());
    }
  }

  private void add(final $Dbcp.Connection.Properties.Property property) {
    final $Dbcp.Connection.Properties.Property.Name$ name = property.getName$();
    final $Dbcp.Connection.Properties.Property.Value$ value = property.getValue$();
    if (name != null && value != null)
      dataSource.addConnectionProperty(name.text(), value.text());
  }

  private void add(final Dbcp.Connection.Properties.Property property) {
    final String name = property.getName();
    final String value = property.getValue();
    if (name != null && value != null)
      dataSource.addConnectionProperty(name, value);
  }

  private DataSources(final String id, final ClassLoader driverClassLoader, final $Dbcp ... dbcps) {
    this.driverClassLoader = driverClassLoader;
    for (final $Dbcp dbcp : dbcps) { // [A]
      final $Dbcp.Id$ id$ = dbcp.getId$();
      if (id != null && (id$ == null || !id.equals(id$.text())))
        continue;

      if (dataSource == null)
        dataSource = new BasicDataSource();

      final $Dbcp.Jdbc jdbc$ = dbcp.getJdbc();
      if (jdbc$ != null) {
        final $QualifiedIdentifier driverClassName$ = jdbc$.getDriverClassName();
        if (driverClassName$ != null)
          driverClassName = driverClassName$.text();

        final $AnyURI url$ = jdbc$.getUrl();
        if (url$ != null)
          url = url$.text().toString();
      }

      final $Dbcp.Default _default = dbcp.getDefault();
      if (_default != null) {
        final $StringNonEmpty catalog$ = _default.getCatalog();
        if (catalog$ != null)
          dataSource.setDefaultCatalog(catalog$.text());

        final $Boolean autoCommit$ = _default.getAutoCommit();
        if (autoCommit$ != null)
          autoCommit = autoCommit$.text();

        final $Boolean readOnly$ = _default.getReadOnly();
        if (readOnly$ != null)
          readOnly = readOnly$.text();

        final $PositiveInt queryTimeout$ = _default.getQueryTimeout();
        if (queryTimeout$ != null)
          queryTimeout = queryTimeout$.text();

        final $Dbcp.Default.TransactionIsolation transactionIsolation$ = _default.getTransactionIsolation();
        if (transactionIsolation$ != null)
          transactionIsolation = transactionIsolation$.text();
      }

      final $Dbcp.Connection connection = dbcp.getConnection();
      if (connection != null) {
        final $Dbcp.Connection.Properties props = connection.getProperties();
        if (props != null) {
          final List<$Dbcp.Connection.Properties.Property> properties = props.getProperty();
          final int i$ = properties.size();
          if (i$ > 0) {
            if (CollectionUtil.isRandomAccess(properties)) {
              int i = 0;
              do // [RA]
                add(properties.get(i));
              while (++i < i$);
            }
            else {
              final Iterator<$Dbcp.Connection.Properties.Property> it = properties.iterator();
              do // [I]
                add(it.next());
              while (it.hasNext());
            }
          }
        }

        final int size;
        final List<$StringNonEmpty> sqls;
        final $Dbcp.Connection.InitSqls initSqls = connection.getInitSqls();
        if (initSqls != null && (size = (sqls = initSqls.getInitSql()).size()) > 0) {
          final String[] sql = new String[size];
          if (CollectionUtil.isRandomAccess(sqls)) {
            int i = 0;
            do // [RA]
              sql[i] = sqls.get(i).text();
            while (++i < size);
          }
          else {
            int i = -1;
            final Iterator<$StringNonEmpty> it = sqls.iterator();
            do // [I]
              sql[++i] = it.next().text();
            while (it.hasNext());
          }

          final List<String> connectionInitSqls = dataSource.getConnectionInitSqls();
          if (connectionInitSqls.size() > 0)
            Collections.addAll(connectionInitSqls, sql);
          else
            dataSource.setConnectionInitSqls(Arrays.asList(sql));
        }
      }

      final $Dbcp.Size size = dbcp.getSize();
      if (size != null) {
        final $NonNegativeInt initialSize$ = size.getInitialSize();
        if (initialSize$ != null)
          initialSize = initialSize$.text();

        final $NonNegativeInt minIdle$ = size.getMinIdle();
        if (minIdle$ != null)
          minIdle = minIdle$.text();

        final $IndefinitePositiveInt maxIdle$ = size.getMaxIdle();
        if (maxIdle$ != null)
          maxIdle = maxIdle$.text();

        final $IndefinitePositiveInt maxTotal$ = size.getMaxTotal();
        if (maxTotal$ != null)
          maxTotal = maxTotal$.text();

        final $Dbcp.Size.PoolPreparedStatements poolPreparedStatements$ = size.getPoolPreparedStatements();
        if (poolPreparedStatements$ != null) {
          poolPreparedStatements = true;
          final $IndefinitePositiveInt maxOpen$ = poolPreparedStatements$.getMaxOpen();
          if (maxOpen$ != null)
            maxOpen = maxOpen$.text();
        }
      }

      final $Dbcp.Pool pool = dbcp.getPool();
      if (pool != null) {
        final $Dbcp.Pool.Queue queue = pool.getQueue();
        if (queue != null) {
          final String text = queue.text();
          if ("lifo".equals(text))
            lifo = true;
          else if ("fifo".equals(text))
            lifo = false;
          else
            throw new UnsupportedOperationException("Unsupported queue spec: " + queue);
        }

        final $Boolean cacheState$ = pool.getCacheState();
        if (cacheState$ != null)
          cacheState = cacheState$.text();

        final $IndefinitePositiveLong maxWait$ = pool.getMaxWait();
        if (maxWait$ != null)
          maxWait = maxWait$.text();

        final $IndefinitePositiveLong maxConnectionLifetime$ = pool.getMaxConnectionLifetime();
        if (maxConnectionLifetime$ != null)
          maxConnLifetime = maxConnectionLifetime$.text();

        final $Boolean autoCommitOnReturn$ = pool.getAutoCommitOnReturn();
        if (autoCommitOnReturn$ != null)
          autoCommitOnReturn = autoCommitOnReturn$.text();

        final $Boolean rollbackOnReturn$ = pool.getRollbackOnReturn();
        if (rollbackOnReturn$ != null)
          rollbackOnReturn = rollbackOnReturn$.text();

        final $Dbcp.Pool.RemoveAbandoned removeAbandoned$ = pool.getRemoveAbandoned();
        if (removeAbandoned$ != null) {
          removeAbandonedOn = removeAbandoned$.getOn$().text();
          removeAbandonedTimeout = removeAbandoned$.getTimeout$().text();
        }

        final $Boolean abandonedUsageTracking$ = pool.getAbandonedUsageTracking();
        if (abandonedUsageTracking$ != null)
          abandonedUsageTracking = abandonedUsageTracking$.text();

        final $Boolean allowAccessToUnderlyingConnection$ = pool.getAllowAccessToUnderlyingConnection();
        if (allowAccessToUnderlyingConnection$ != null)
          accessToUnderlyingConnectionAllowed = allowAccessToUnderlyingConnection$.text();

        final $Dbcp.Pool.Eviction eviction = pool.getEviction();
        if (eviction != null) {
          hasEviction = true;
          final $IndefinitePositiveLong timeBetweenRuns$ = eviction.getTimeBetweenRuns();
          if (timeBetweenRuns$ != null)
            timeBetweenEvictionRunsMillis = timeBetweenRuns$.text();

          final $NonNegativeInt numTestsPerRun$ = eviction.getNumTestsPerRun();
          if (numTestsPerRun$ != null)
            numTestsPerRun = numTestsPerRun$.text();

          final $NonNegativeLong minIdleTime$ = eviction.getMinIdleTime();
          if (minIdleTime$ != null)
            minEvictableIdleTimeMillis = minIdleTime$.text();

          final $IndefinitePositiveLong softMinIdleTime$ = eviction.getSoftMinIdleTime();
          if (softMinIdleTime$ != null)
            softMinEvictableIdleTimeMillis = softMinIdleTime$.text();

          final $QualifiedIdentifier policyClassName$ = eviction.getPolicyClassName();
          if (policyClassName$ != null)
            policyClassName = policyClassName$.text();
        }
      }

      final $Dbcp.Validation validation = dbcp.getValidation();
      if (validation != null) {
        hasValidation = true;
        final $Dbcp.Validation.Query query$ = validation.getQuery();
        if (query$ != null)
          validationQuery = query$.text();

        final $IndefinitePositiveInt timeout$ = validation.getTimeout();
        if (timeout$ != null)
          validationQueryTimeout = timeout$.text();

        final $Boolean testOnCreate$ = validation.getTestOnCreate();
        if (testOnCreate$ != null)
          testOnCreate = testOnCreate$.text();

        final $Boolean testOnBorrow$ = validation.getTestOnBorrow();
        if (testOnBorrow$ != null)
          testOnBorrow = testOnBorrow$.text();

        final $Boolean testOnReturn$ = validation.getTestOnReturn();
        if (testOnReturn$ != null)
          testOnReturn = testOnReturn$.text();

        final $Boolean testWhileIdle$ = validation.getTestWhileIdle();
        if (testWhileIdle$ != null)
          testWhileIdle = testWhileIdle$.text();

        final $Dbcp.Validation.FastFail failFast = validation.getFastFail();
        if (failFast != null) {
          dataSource.setFastFailValidation(true);
          final $StringNonEmpty disconnectionSqlCodes$ = failFast.getDisconnectionSqlCodes();
          final String text;
          if (disconnectionSqlCodes$ != null && (text = disconnectionSqlCodes$.text().trim()).length() > 0) {
            if (disconnectionQueryCodes == null)
              disconnectionQueryCodes = new ArrayList<>();

            Collections.addAll(disconnectionQueryCodes, Strings.split(text, ' '));
          }
        }
      }

      final $Dbcp.Logging logging = dbcp.getLogging();
      if (logging != null) {
        loggingLevel = logging.getLevel().text();
        final $Boolean logExpiredConnections$ = logging.getLogExpiredConnections();
        if (logExpiredConnections$ != null)
          logExpiredConnections = logExpiredConnections$.text();

        final $Boolean logAbandoned$ = logging.getLogAbandoned();
        if (logAbandoned$ != null)
          logAbandoned = logAbandoned$.text();
      }

      if (dbcp.getJmxName() != null)
        dataSource.setJmxName(dbcp.getJmxName().text());
    }
  }

  private BasicDataSource build() {
    if (dataSource == null)
      return null;

    if (driverClassName == null)
      throw new IllegalArgumentException("/dbcp:jdbc is missing");

    dataSource.setDriverClassName(driverClassName);
    dataSource.setDriverClassLoader(driverClassLoader);
    dataSource.setUrl(url);

    dataSource.setDefaultAutoCommit(autoCommit);
    dataSource.setDefaultReadOnly(readOnly);
    dataSource.setDefaultQueryTimeout(queryTimeout);

    if (transactionIsolation != null) {
      if ("NONE".equals(transactionIsolation))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
      else if ("READ_UNCOMMITTED".equals(transactionIsolation))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
      else if ("READ_COMMITTED".equals(transactionIsolation))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      else if ("REPEATABLE_READ".equals(transactionIsolation))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
      else if ("SERIALIZABLE".equals(transactionIsolation))
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      else
        throw new UnsupportedOperationException("Unsupported transaction isolation: " + transactionIsolation);
    }

    dataSource.setInitialSize(initialSize);
    dataSource.setMinIdle(minIdle);
    dataSource.setMaxIdle(INDEFINITE.equals(maxIdle) ? DEFAULT_MAX_TOTAL : Integer.parseInt(maxIdle));
    dataSource.setMaxTotal(INDEFINITE.equals(maxTotal) ? DEFAULT_MAX_TOTAL : Integer.parseInt(maxTotal));
    dataSource.setPoolPreparedStatements(poolPreparedStatements);
    dataSource.setMaxOpenPreparedStatements(INDEFINITE.equals(maxOpen) ? DEFAULT_MAX_TOTAL : Integer.parseInt(maxOpen));

    dataSource.setLifo(lifo);

    dataSource.setCacheState(cacheState);
    dataSource.setMaxWaitMillis(INDEFINITE.equals(maxWait) ? DEFAULT_MAX_WAIT_MILLIS : Long.parseLong(maxWait));
    dataSource.setMaxConnLifetimeMillis(INDEFINITE.equals(maxConnLifetime) ? -1 : Long.parseLong(maxConnLifetime));
    dataSource.setAutoCommitOnReturn(autoCommitOnReturn);
    dataSource.setRollbackOnReturn(rollbackOnReturn);
    if (removeAbandonedOn != null) {
      if ("borrow".equals(removeAbandonedOn))
        dataSource.setRemoveAbandonedOnBorrow(true);
      else if ("maintenance".equals(removeAbandonedOn))
        dataSource.setRemoveAbandonedOnMaintenance(true);
      else
        throw new UnsupportedOperationException("Unsupported remove abandoned spec: " + removeAbandonedOn);

      dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
    }
    dataSource.setAbandonedUsageTracking(abandonedUsageTracking);
    dataSource.setAccessToUnderlyingConnectionAllowed(accessToUnderlyingConnectionAllowed);

    if (hasEviction) {
      dataSource.setTimeBetweenEvictionRunsMillis(INDEFINITE.equals(timeBetweenEvictionRunsMillis) ? DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS : Long.parseLong(timeBetweenEvictionRunsMillis));
      dataSource.setNumTestsPerEvictionRun(numTestsPerRun);
      dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
      dataSource.setSoftMinEvictableIdleTimeMillis(INDEFINITE.equals(softMinEvictableIdleTimeMillis) ? DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS : Long.parseLong(softMinEvictableIdleTimeMillis));
      if (policyClassName != null)
        dataSource.setEvictionPolicyClassName(policyClassName);
    }

    if (hasValidation) {
      if (validationQuery != null)
        dataSource.setValidationQuery(validationQuery);

      dataSource.setValidationQueryTimeout(INDEFINITE.equals(validationQueryTimeout) ? -1 : Integer.parseInt(validationQueryTimeout));
      dataSource.setTestOnCreate(testOnCreate);
      dataSource.setTestOnBorrow(testOnBorrow);
      dataSource.setTestOnReturn(testOnReturn);
      dataSource.setTestWhileIdle(testWhileIdle);

      if (dataSource.getFastFailValidation())
        dataSource.setDisconnectionSqlCodes(disconnectionQueryCodes != null ? disconnectionQueryCodes : defaultDisconnectionQueryCodes);
    }

    if (loggingLevel != null) {
      final Logger logger = LoggerFactory.getLogger(DataSources.class);
      final LoggerPrintWriter loggerPrintWriter = new LoggerPrintWriter(logger, Level.valueOf(loggingLevel));
      try {
        dataSource.setLogWriter(loggerPrintWriter);
      }
      catch (final SQLException e) {
        throw new RuntimeException(e); // Will not occur, because this behavior has been overridden.
      }

      dataSource.setLogExpiredConnections(logExpiredConnections);
      if (logAbandoned) {
        dataSource.setAbandonedLogWriter(loggerPrintWriter);
        dataSource.setLogAbandoned(true);
      }
    }

    return dataSource;
  }
}