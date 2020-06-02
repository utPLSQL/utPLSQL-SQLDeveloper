/*
 * Copyright 2020 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.utplsql.sqldev.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.utplsql.sqldev.exception.GenericDatabaseAccessException;
import org.utplsql.sqldev.exception.GenericRuntimeException;

import oracle.dbtools.raptor.navigator.db.DatabaseConnection;
import oracle.dbtools.raptor.utils.Connections;
import oracle.javatools.db.DBException;
import oracle.jdeveloper.db.ConnectionException;

public class DatabaseTools {
    // do not instantiate this class
    private DatabaseTools() {
        super();
    }

    public static Connection getConnection(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new GenericDatabaseAccessException("Error getting connection.", e);
        }
    }
    
    public static Connection getConnection(DatabaseConnection conn) {
        try {
            return conn.getConnection();
        } catch (IOException e) {
            final String msg = "Error getting connection for " + conn.getConnectionName() + ".";
            throw new GenericDatabaseAccessException(msg, e);
        }
    }

    public static Connection getConnection(String connectionName) {
        try {
            return Connections.getInstance().getConnection(connectionName);
        } catch (DBException e) {
            final String msg = "Error getting connection for " + connectionName + ".";
            throw new GenericDatabaseAccessException(msg, e);
        }
    }
    
    public static Connection cloneConnection(String connectionName) {
        final Connection conn = getConnection(connectionName);
        try {
            return Connections.getInstance().cloneConnection(conn);
        } catch (ConnectionException e) {
            final String msg = "Error cloning connection " + connectionName + ".";
            throw new GenericDatabaseAccessException(msg, e);
        }
    }
    
    private static String createTemporaryConnection(String connectionName) {
        try {
            return Connections.getInstance().createTemporaryConnection(connectionName);
        } catch (Throwable e) {
            final String msg = "Error creating temporary connection based on " + connectionName + ".";
            throw new GenericDatabaseAccessException(msg, e);
        }
    }
    
    private static String createPrivateConnection(String connectionName) {
        try {
            return Connections.getInstance().createPrivateConnection(connectionName);
        } catch (Throwable e) {
            final String msg = "Error creating private connection based on " + connectionName + ".";
            throw new GenericDatabaseAccessException(msg, e);
        }
    }
    
    public static String createTemporaryOrPrivateConnection(String connectionName) {
        // Private connections are closed in SQL Developer < 17.4.0 when the worksheet
        // is closed, but in SQL Developer > 17.4.0 private connections are not closed.
        // Temporary connections have been introduced in SQL Developer 17.4.0. They will
        // be always closed, when a worksheet is closed.
        // Hence we try to use temporary connections whenever possible. See also
        // https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/47 .
        try {
            return createTemporaryConnection(connectionName);
        } catch (GenericDatabaseAccessException e) {
            return createPrivateConnection(connectionName);
        }
    }
    
    public static void closeConnection(Connection conn) {
        abortConnection(conn);
    }
    
    public static void abortConnection(Connection conn) {
        final SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        try {
            conn.abort(taskExecutor);
        } catch (SQLException e) {
            throw new GenericDatabaseAccessException("Could not abort connection.");
        }
    }
    
    public static String getSchema(Connection conn) {
        try {
            return conn.getSchema();
        } catch (SQLException e) {
            throw new GenericRuntimeException("Error getting schema name of connection.", e);
        }
    }

    public static String getUser(Connection conn) {
        try {
            return conn.getMetaData().getUserName();
        } catch (SQLException e) {
            throw new GenericRuntimeException("Error getting user name of connection.", e);
        }
    }   
    
    public static String getSchema(DatabaseConnection conn) {
        return getSchema(getConnection(conn));
    }

    
    public static String getSchema(String connectionName) {
        return getSchema(getConnection(connectionName));
    }
    
    public static boolean isSupported(final Connection conn) {
        try {
            boolean ret = false;
            if (conn != null && conn.getMetaData().getDatabaseProductName().startsWith("Oracle")
                    && (conn.getMetaData().getDatabaseMajorVersion() == 11
                            && conn.getMetaData().getDatabaseMinorVersion() >= 2
                            || conn.getMetaData().getDatabaseMajorVersion() > 11)) {
                ret = true;
            }
            return ret;
        } catch (SQLException e) {
            throw new GenericDatabaseAccessException("Error while getting product version of connection.", e);
        }
    }
}
