/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    /**
     * Split a string by one separator character. The performance
     * is better than Java String split.
     * 
     * @param str is the string need be split.
     * @param separatorChar the single separator character.
     * @return the array of split items.
     */
    public static String[] split(String str, char separatorChar) {
        if (str == null) {
            return null;
        }

        int length = str.length();

        if (length == 0) {
            return null;
        }

        List<String> list = new ArrayList<String>();
        int i = 0;
        int start = 0;
        boolean match = false;

        while (i < length) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                }

                start = ++i;
                continue;
            }

            match = true;
            i++;
        }

        if (match) {
            list.add(str.substring(start, i));
        }

        return (String[]) list.toArray(new String[list.size()]);
    }
}
