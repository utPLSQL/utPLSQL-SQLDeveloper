/*
 * Copyright 2019 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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

import java.util.List;

//converted to Xtend based on Java code on https://www.geeksforgeeks.org/longest-common-prefix-using-binary-search/
//converted back to Java with some amendments
public class PrefixTools {
    
    // do not instantiate this class
    private PrefixTools() {
        super();
    }
    
    public static int findMinLength(final String[] arr, final int n) {
        int min = Integer.MAX_VALUE;
        for (int i=0; i < n; i++) {
            if (arr[i].length() < min) {
                min = arr[i].length();
            }
        }
        return min;
    }

    public static boolean allContainsPrefix(final String[] arr, final int n, final String str, final int start, final int end) {
        for (int i=0; i < n; i++) {
            String item = arr[i];
            for (int j = start; j <= end; j++) {
                if (item.charAt(j) != str.charAt(j)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String commonPrefix(final String[] arr, final int n) {
        int index = findMinLength(arr, n);
        StringBuilder prefix = new StringBuilder();
        int low = 0;
        int high = index; // index-1 is wrong
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (allContainsPrefix(arr, n, arr[0], low, mid)) {
                prefix.append(arr[0].substring(low, mid + 1));
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return prefix.toString();
    }

    public static String commonPrefix(final List<String> list) {
        try {
            if (list.isEmpty()) {
                return "";
            } else if (list.size() == 1) {
                final int pos = list.get(0).lastIndexOf('.');
                if (pos > 0) {
                    return list.get(0).substring(0, pos + 1);
                } else {
                    return "";
                }
            } else {
                final String[] testArray = new String[list.size()];
                final String prefix = commonPrefix(list.toArray(testArray), list.size());
                return prefix;
            }

        } catch (final Exception e) {
            return "";
        }
    }
}
