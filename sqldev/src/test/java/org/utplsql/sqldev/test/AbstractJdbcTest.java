/**
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

import com.google.common.base.Objects;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Properties;
import oracle.dbtools.raptor.newscriptrunner.ISQLCommand;
import oracle.dbtools.raptor.newscriptrunner.SQLCommand;
import oracle.dbtools.worksheet.scriptparser.sqlplus.SQLPlusScriptParser;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@SuppressWarnings("all")
public abstract class AbstractJdbcTest {
  protected static SingleConnectionDataSource dataSource;
  
  protected static JdbcTemplate jdbcTemplate;
  
  protected static SingleConnectionDataSource sysDataSource;
  
  protected static JdbcTemplate sysJdbcTemplate;
  
  protected static final JdbcTemplate _staticInitializerForDataSourceAndJdbcTemplate = new Function0<JdbcTemplate>() {
    public JdbcTemplate apply() {
      try {
        JdbcTemplate _xblockexpression = null;
        {
          final Properties p = new Properties();
          p.load(AbstractJdbcTest.class.getClass().getResourceAsStream("/test.properties"));
          SingleConnectionDataSource _singleConnectionDataSource = new SingleConnectionDataSource();
          AbstractJdbcTest.dataSource = _singleConnectionDataSource;
          AbstractJdbcTest.dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("jdbc:oracle:thin:@");
          String _property = p.getProperty("host");
          _builder.append(_property);
          _builder.append(":");
          String _property_1 = p.getProperty("port");
          _builder.append(_property_1);
          _builder.append("/");
          String _property_2 = p.getProperty("service");
          _builder.append(_property_2);
          AbstractJdbcTest.dataSource.setUrl(_builder.toString());
          AbstractJdbcTest.dataSource.setUsername(p.getProperty("scott_username"));
          AbstractJdbcTest.dataSource.setPassword(p.getProperty("scott_password"));
          JdbcTemplate _jdbcTemplate = new JdbcTemplate(AbstractJdbcTest.dataSource);
          AbstractJdbcTest.jdbcTemplate = _jdbcTemplate;
          SingleConnectionDataSource _singleConnectionDataSource_1 = new SingleConnectionDataSource();
          AbstractJdbcTest.sysDataSource = _singleConnectionDataSource_1;
          AbstractJdbcTest.sysDataSource.setDriverClassName("oracle.jdbc.OracleDriver");
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("jdbc:oracle:thin:@");
          String _property_3 = p.getProperty("host");
          _builder_1.append(_property_3);
          _builder_1.append(":");
          String _property_4 = p.getProperty("port");
          _builder_1.append(_property_4);
          _builder_1.append("/");
          String _property_5 = p.getProperty("service");
          _builder_1.append(_property_5);
          AbstractJdbcTest.sysDataSource.setUrl(_builder_1.toString());
          AbstractJdbcTest.sysDataSource.setUsername(p.getProperty("sys_username"));
          AbstractJdbcTest.sysDataSource.setPassword(p.getProperty("sys_password"));
          JdbcTemplate _jdbcTemplate_1 = new JdbcTemplate(AbstractJdbcTest.sysDataSource);
          _xblockexpression = AbstractJdbcTest.sysJdbcTemplate = _jdbcTemplate_1;
        }
        return _xblockexpression;
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    }
  }.apply();
  
  public static ArrayList<String> getStatements(final String sqlplusScript) {
    StringReader _stringReader = new StringReader(sqlplusScript);
    SQLPlusScriptParser p = new SQLPlusScriptParser(_stringReader);
    final ArrayList<String> stmts = new ArrayList<String>();
    while (p.hasNext()) {
      {
        final ISQLCommand stmt = p.next();
        if (((((stmt.getExecutable() || stmt.getRunnable()) && (!Objects.equal(stmt.getStmtType(), SQLCommand.StmtType.G_C_COMMENT))) && 
          (!Objects.equal(stmt.getStmtType(), SQLCommand.StmtType.G_C_MULTILINECOMMENT))) && (!Objects.equal(stmt.getStmtType(), SQLCommand.StmtType.G_C_SQLPLUS)))) {
          stmts.add(stmt.getSql());
        }
      }
    }
    return stmts;
  }
}
