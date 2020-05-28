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

import org.utplsql.sqldev.exception.GenericDatabaseAccessException;

import oracle.dbtools.raptor.navigator.db.DatabaseConnection;
import oracle.dbtools.raptor.utils.Connections;
import oracle.javatools.db.DBException;

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
}
