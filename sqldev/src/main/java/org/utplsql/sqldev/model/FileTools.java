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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.utplsql.sqldev.exception.GenericRuntimeException;

public class FileTools {
    // do not instantiate this class
    private FileTools() {
        super();
    }

    public static byte[] readFile(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            final String msg = "Cannot read file " + path.toString() + ".";
            throw new GenericRuntimeException(msg, e);
        }
    }

    public static void writeFile(Path path, byte[] bytes) {
        try {
            Files.write(path, bytes);
        } catch (IOException e) {
            final String msg = "Cannot write file " + path.toString() + ".";
            throw new GenericRuntimeException(msg, e);
        }
    }

    public static void writeFile(Path path, Iterable<? extends CharSequence> lines, Charset cs) {
        try {
            Files.write(path, lines, cs);
        } catch (IOException e) {
            final String msg = "Cannot write file " + path.toString() + ".";
            throw new GenericRuntimeException(msg, e);
        }
    }
}
