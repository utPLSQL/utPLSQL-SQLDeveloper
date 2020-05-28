/*
 * Copyright 2018 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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
package org.utplsql.sqldev.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.utplsql.sqldev.exception.GenericRuntimeException;

import oracle.dbtools.raptor.newscriptrunner.ISQLCommand;
import oracle.dbtools.raptor.newscriptrunner.SQLCommand;
import oracle.dbtools.worksheet.scriptparser.sqlplus.SQLPlusScriptParser;

public abstract class AbstractJdbcTest {
    protected static SingleConnectionDataSource dataSource;
    protected static JdbcTemplate jdbcTemplate;
    protected static SingleConnectionDataSource sysDataSource;
    protected static JdbcTemplate sysJdbcTemplate;

    static {
        final Properties p = new Properties();
        try {
            p.load(AbstractJdbcTest.class.getClass().getResourceAsStream("/test.properties"));
        } catch (IOException e) {
            throw new GenericRuntimeException("Cannot read test.properties", e);
        }
        // create dataSource and jdbcTemplate
        dataSource = new SingleConnectionDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setUrl("jdbc:oracle:thin:@" + p.getProperty("host") + ":" + p.getProperty("port") + "/"
                + p.getProperty("service"));
        dataSource.setUsername(p.getProperty("scott_username"));
        dataSource.setPassword(p.getProperty("scott_password"));
        jdbcTemplate = new JdbcTemplate(dataSource);
        // create dbaDataSource and dbaJdbcTemplate
        sysDataSource = new SingleConnectionDataSource();
        sysDataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        sysDataSource.setUrl("jdbc:oracle:thin:@" + p.getProperty("host") + ":" + p.getProperty("port") + "/"
                + p.getProperty("service"));
        sysDataSource.setUsername(p.getProperty("sys_username"));
        sysDataSource.setPassword(p.getProperty("sys_password"));
        sysJdbcTemplate = new JdbcTemplate(sysDataSource);
    }

    public static List<String> getStatements(final String sqlplusScript) {
        SQLPlusScriptParser p = new SQLPlusScriptParser(new StringReader(sqlplusScript));
        final ArrayList<String> stmts = new ArrayList<>();
        while (p.hasNext()) {
            final ISQLCommand stmt = p.next();
            if ((stmt.getExecutable() || stmt.getRunnable()) && stmt.getStmtType() != SQLCommand.StmtType.G_C_COMMENT
                    && stmt.getStmtType() != SQLCommand.StmtType.G_C_MULTILINECOMMENT
                    && stmt.getStmtType() != SQLCommand.StmtType.G_C_SQLPLUS) {
                stmts.add(stmt.getSql());
            }
        }
        return stmts;
    }
}
