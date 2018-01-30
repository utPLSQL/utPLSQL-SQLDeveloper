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
 package org.utplsql.sqldev.dal

import java.sql.Connection
import java.util.List
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import org.utplsql.sqldev.model.ut.Annotation

class UtplsqlDao {
	public static val UTPLSQL_PACKAGE_NAME = "UT" 
	private var Connection conn
	private var JdbcTemplate jdbcTemplate

	new(Connection conn) {
		this.conn = conn
		this.jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(conn, true))
	}
	
	/**
	 * Gets the schema name of the utPLSQL installation.
	 * 
	 * @return utPLSQL schema or null if no utPLSQL is not installed
	 * @throws DataAccessException if there is a problem
	 */
	def String getUtplsqlSchema() {
		val sql = '''
			SELECT table_owner
			  FROM all_synonyms
			 WHERE owner = 'PUBLIC'
			   AND synonym_name = '«UTPLSQL_PACKAGE_NAME»'
			   AND table_name = '«UTPLSQL_PACKAGE_NAME»'
		'''
		try {
			val schema = jdbcTemplate.queryForObject(sql, String)
			return schema
		} catch (EmptyResultDataAccessException e) {
			return null	
		}
	}
	
	/**
	 * Checks if the package ut_annotation_manager is installed.
	 * This package has been introduced with utPLSQL 3.0.4.
	 * This version is a prerequisite to identify 
	 * utPLSQL unit test procedures.
	 * 
	 * @return true if ut_annotation_manager package has been found
	 * @throws DataAccessException if there is a problem
	 */
	def boolean isUtAnnotationManagerInstalled() {
		if (utplsqlSchema !== null) {
			val sql = '''
				SELECT count(*)
				  FROM all_objects
				 WHERE owner = '«utplsqlSchema»'
				   AND object_type = 'PACKAGE'
				   AND object_name = 'UT_ANNOTATION_MANAGER'
			'''
			val found = jdbcTemplate.queryForObject(sql, Integer)
			return found == 1
		}
		return false
	}
	
	/**
	 * Checks if utPLSQL tests exist
	 * 
	 * @param owner schema name, mandatory, case-insensitive
	 * @param objectName name of the package or package body, optional, case-insensitive
	 * @param subobjectName name of the procedure, optional, case-insensitive
	 * @return true if at least one test has been found
	 * @throws DataAccessException if a utPLSQL version less than 3.0.4 is installed or if there are other problems
	 */
	def boolean containsUtplsqlTest(String owner, String objectName, String subobjectName) {
		try {
			val sql = '''
				SELECT count(
				          CASE 
				             WHEN a.name = 'test' 
				                  AND (upper(a.subobject_name) = upper(?) OR ? IS NULL) 
				             THEN 
				               1 
				             ELSE 
				               NULL 
				          END
				       )
				  FROM TABLE(«utplsqlSchema».ut_annotation_manager.get_annotated_objects(upper(?), 'PACKAGE')) o
				 CROSS JOIN TABLE(o.annotations) a
				 WHERE (o.object_name = upper(?) OR ? IS NULL)
				   AND a.name IN ('test', 'suite')
				HAVING count(
				          CASE 
				             WHEN a.name = 'suite' THEN 
				                1 
				             ELSE 
				                NULL 
				          END
				       ) > 0
			'''
			val found = jdbcTemplate.queryForObject(sql, Integer, #[subobjectName, subobjectName, owner, objectName, objectName])
			return found > 0
		} catch (EmptyResultDataAccessException e) {
			return false	
		}
	} 
	
	def boolean containsUtplsqlTest(String owner) {
		return containsUtplsqlTest(owner, null, null)
	}
	
	def boolean containsUtplsqlTest(String owner, String objectType) {
		return containsUtplsqlTest(owner, objectType, null)
	}
	
	/**
	 * Gets a list of utPLSQL annotations for a given PL/SQL package specification
	 * 
	 * @param owner schema name, mandatory, case-insensitive
	 * @param objectName name of the package or package body, optional, case-insensitive
	 * @return list of Annotation with name 'suite' or 'test'
	 * @throws DataAccessException if a utPLSQL version less than 3.0.4 is installed or if there are other problems
	 */
	def List<Annotation> annotations(String owner, String objectName) {
		val sql = '''
			SELECT o.object_owner, o.object_type, o.object_name, a.name, a.text, a.subobject_name
			  FROM TABLE(«utplsqlSchema».ut_annotation_manager.get_annotated_objects(upper(?), 'PACKAGE')) o
			 CROSS JOIN TABLE(o.annotations) a
			 WHERE o.object_name = upper(?)
		'''
		val result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<Annotation>(Annotation), #[owner, objectName])
		return result
	}
	
}