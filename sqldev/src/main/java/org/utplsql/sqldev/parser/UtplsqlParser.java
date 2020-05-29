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
package org.utplsql.sqldev.parser;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.JTextComponent;

import org.utplsql.sqldev.dal.UtplsqlDao;
import org.utplsql.sqldev.exception.GenericRuntimeException;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.model.parser.PlsqlObject;
import org.utplsql.sqldev.model.parser.Unit;
import org.utplsql.sqldev.model.ut.Annotation;

public class UtplsqlParser {
    private String owner;
    private String plsql;
    private String plsqlReduced;
    private ArrayList<PlsqlObject> objects = new ArrayList<>();
    private ArrayList<Unit> units = new ArrayList<>();

    public UtplsqlParser(final String plsql, final Connection conn, final String owner) {
        setPlsql(plsql);
        setPlsqlReduced();
        populateObjects();
        populateUnits();
        processAnnotations(conn, owner);
    }

    public UtplsqlParser(final String plsql) {
        this(plsql, null, null);
    }

    /**
     * JTextComponents uses one position for EOL (end-of-line),
     * even on Windows platforms were it is two characters (CR/LF).
     * To simplify position calculations and subsequent regular expressions
     * all new lines are replaced with LF on Windows platforms.
     */
    private void setPlsql(final String plsql) {
        final String lineSep = System.getProperty("line.separator");
        if (!"\n".equals(lineSep)) {
            // replace CR/LF with LF on Windows platforms
            this.plsql = plsql.replace(lineSep, "\n");
        } else {
            this.plsql = plsql;
        }
    }

    /**
     * replace the following expressions with space to simplify 
     * and improve performance of subsequent regular expressions:
     * - multi-line PL/SQL comments
     * - single-line PL/SQL comments
     * - string literals
     * the result is not valid PL/SQL anymore, but good enough
     * to find PL/SQL objects and units
     */
    private void setPlsqlReduced() {
        final StringBuilder sb = new StringBuilder();
        final Pattern p = Pattern.compile("(/\\*(.|[\\n])*?\\*/)|(--[^\\n]*\\n)|(\'([^\']|[\\n])*?\')");
        final Matcher m = p.matcher(plsql);
        int pos = 0;
        while (m.find()) {
            if (pos < m.start()) {
                sb.append(plsql.substring(pos, m.start()));
            }
            for (int i = m.start(); i < m.end(); i++) {
                final String c = plsql.substring(i, i + 1);
                if ("\n".equals(c) || "\r".equals(c)) {
                    sb.append(c);
                } else {
                    sb.append(' ');
                }
            }
            pos = m.end();
        }
        if (plsql.length() > pos) {
            sb.append(plsql.substring(pos, plsql.length()));
        }
        plsqlReduced=sb.toString();
    }

    private void populateObjects() {
        final Pattern p = Pattern.compile(
                "(?i)(\\s*)(create(\\s+or\\s+replace)?\\s+(package|type|function|procedure)\\s+(body\\s+)?)([^\\s]+)(\\s+)");
        final Matcher m = p.matcher(plsqlReduced);
        while (m.find()) {
            final PlsqlObject o = new PlsqlObject();
            o.setType(m.group(4).toUpperCase());
            o.setName(m.group(6));
            o.setPosition(m.start());
            objects.add(o);
        }
    }

    private void populateUnits() {
        final Pattern p = Pattern.compile("(?i)(\\s*)(procedure)(\\s+)([^\\s\\(;]+)");
        final Matcher m = p.matcher(plsqlReduced);
        while (m.find()) {
            final Unit u = new Unit();
            u.setName(m.group(4));
            u.setPosition(m.start());
            u.setPositionOfName(m.start(4));
            units.add(u);
        }
    }
    
    private void processAnnotations(final Connection conn, final String owner) {
        this.owner = owner;
        if (conn != null) {
            final UtplsqlDao dao = new UtplsqlDao(conn);
            if (dao.isUtAnnotationManagerInstalled()) {
                for (final PlsqlObject o : objects) {
                    final List<String> segments = Arrays.asList(fixName(o.getName()).split("\\."));
                    final String schema = owner != null ? owner : DatabaseTools.getSchema(conn);
                    final List<Annotation> annotations = dao.annotations(schema,
                            segments.get(segments.size() - 1).toUpperCase());
                    if (annotations.stream().anyMatch(it -> it.getName().equals("suite"))) {
                        o.setAnnotations(annotations);
                    }
                }
                final ArrayList<Unit> fixedUnits = new ArrayList<>();
                for (final Unit u : units) {
                    final PlsqlObject o = getObjectAt(u.getPosition());
                    if (o != null && o.getAnnotations() != null
                            && o.getAnnotations().stream().anyMatch(it -> "test".equals(it.getName())
                                    && it.getSubobjectName().equalsIgnoreCase(fixName(u.getName())))) {
                        fixedUnits.add(u);
                    }
                }
                units = fixedUnits;
                final ArrayList<PlsqlObject> fixedObjects = new ArrayList<>();
                for (final PlsqlObject o : objects) {
                    if (o.getAnnotations() != null) {
                        fixedObjects.add(o);
                    }
                }
                objects = fixedObjects;
            }
        }
    }

    /**
     * gets the PL/SQL object based on the current editor position
     * 
     * @param position
     *            the absolute position as used in
     *            {@link JTextComponent#getCaretPosition()}
     * @return the PL/SQL object
     */
    public PlsqlObject getObjectAt(final int position) {
        PlsqlObject obj = null;
        for (final PlsqlObject o : objects) {
            if (o.getPosition() <= position) {
                obj = o;
            }
        }
        return obj;
    }

    /**
     * converts a line and column to a postion as used in as used in
     * {@link JTextComponent#getCaretPosition()} used for testing purposes only
     * 
     * @param line
     *            the line as used in SQL Developer, starting with 1
     * @param column
     *            the column as used in SQL Developer, starting with 1
     * @return the position
     */
    public int toPosition(final int line, final int column) {
        int lines = 0;
        for (int i = 0; i < plsql.length(); i++) {
            if ("\n".equals(plsql.substring(i, i + 1))) {
                lines++;
                if (lines == line - 1) {
                    return i + column;
                }
            }
        }
        throw new GenericRuntimeException("Line " + line + " not found.");
    }

    private String getUnitNameAt(final int position) {
        String name = "";
        for (final Unit u : units) {
            if (u.getPosition() <= position) {
                name = u.getName();
            }
        }
        return name;
    }

    private String fixName(final String name) {
        return name.replace("\"", "");
    }

    public List<PlsqlObject> getObjects() {
        return objects;
    }

    public List<Unit> getUnits() {
        return units;
    }

    /**
     * gets the utPLSQL path based on the current editor position
     * 
     * @param position
     *            the absolute position as used in
     *            {@link JTextComponent#getCaretPosition()}
     * @return the utPLSQL path
     */
    public String getPathAt(final int position) {
        final StringBuilder sb = new StringBuilder();
        final PlsqlObject object = getObjectAt(position);
        if (object != null && "PACKAGE".equals(object.getType())) {
            final String unitName = getUnitNameAt(position);
            if (owner != null) {
                sb.append(owner);
                sb.append(".");
            }
            sb.append(fixName(object.getName()));
            if (!unitName.isEmpty()) {
                sb.append(".");
                sb.append(fixName(unitName));
            }
        }
        return sb.toString();
    }

    private int getStartLine(final int position) {
        int line = 1;
        for (int i = 0; i < plsql.length(); i++) {
            final String c = plsql.substring(i, i + 1);
            if (i > position) {
                return line;
            } else if ("\n".equals(c)) {
                line++;
            }
        }
        return line;
    }

    /**
     * get the line of a PL/SQL package unit
     * 
     * @param unitName
     *            name of the unit. Only procedures are supported
     * @return the line where the procedure is defined
     */
    public int getLineOf(final String unitName) {
        for (final Unit u : units) {
            if (u.getName().equalsIgnoreCase(unitName)) {
                return getStartLine(u.getPositionOfName());
            }
        }
        return 1;
    }
}
