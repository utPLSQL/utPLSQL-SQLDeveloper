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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import oracle.dbtools.parser.LexerToken;
import oracle.dbtools.raptor.navigator.plsql.Member;
import oracle.dbtools.raptor.navigator.plsql.PlSqlArguments;
import oracle.dbtools.raptor.navigator.plsql.PlsqlStructureParser;

/*
 * Cannot use this class within SQL Developer because the
 * package oracle.dbtools.parser is not exported in sqldeveloper OSGI bundle (extension)
 * (throws ClassNotFoundException at runtime).
 * 
 * The dbtools-common.jar contains the necessary packages,
 * but it cannot be distributed with the utPLSQL extension
 * without violating the Oracle license agreement.
 */
public class SqlDevParser {

    @SuppressWarnings("unchecked")
    public Set<Member> getMembers(final String plsql) {
        final List<LexerToken> tokens = LexerToken.parse(plsql);
        final PlsqlStructureParser parser = new PlsqlStructureParser();
        parser.parse(tokens, PlSqlArguments.getSort());
        return parser.children;
    }

    private int getStartLine(final String plsql, final int offset) {
        int line = 1;
        for (int i = 0; i < plsql.length(); i++) {
            final String c = plsql.substring(i, i + 1);
            if (i > offset) {
                return line;
            } else if ("\n".equals(c)) {
                line = line + 1;
            }
        }
        return line;
    }

    public int getMemberStartLine(final String plsql, final String memberName) {
        final Set<Member> members = this.getMembers(plsql);
        final Optional<Member> member = members.stream().filter(it -> it.name.equalsIgnoreCase(memberName)).findFirst();
        if (member.isPresent()) {
            return this.getStartLine(plsql, member.get().codeOffset);
        } else {
            return 1;
        }
    }
}
