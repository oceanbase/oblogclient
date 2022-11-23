/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.common.util;


import java.lang.reflect.Field;
import org.apache.commons.lang3.StringUtils;

/** Utils class to check and convert data types. */
public class TypeTrait {

    /**
     * Checks if it is a number type object.
     *
     * @param obj An object to check.
     * @return True if it is a number type object, false otherwise.
     */
    public static boolean isNumber(Object obj) {
        return (obj instanceof Byte)
                || (obj instanceof Short)
                || (obj instanceof Integer)
                || (obj instanceof Long);
    }

    /**
     * Checks if it is a number type {@link Field}.
     *
     * @param field A field to check.
     * @return True if it is a number type field, false otherwise.
     */
    public static boolean isNumber(Field field) {
        String typeName = field.getGenericType().getTypeName();
        return "byte".equals(typeName)
                || "java.lang.Byte".equals(typeName)
                || "short".equals(typeName)
                || "java.lang.Short".equals(typeName)
                || "int".equals(typeName)
                || "java.lang.Integer".equals(typeName)
                || "long".equals(typeName)
                || "java.lang.Long".equals(typeName);
    }

    /**
     * Checks if it is a real number type object.
     *
     * @param obj An object to check.
     * @return True if it is a real number type object, false otherwise.
     */
    public static boolean isReal(Object obj) {
        return (obj instanceof Float) || (obj instanceof Double);
    }

    /**
     * Checks if it is a real number type {@link Field}.
     *
     * @param field A field to check.
     * @return True if it is a real number type field, false otherwise.
     */
    public static boolean isReal(Field field) {
        String typeName = field.getGenericType().getTypeName();
        return "float".equals(typeName)
                || "java.lang.Float".equals(typeName)
                || "double".equals(typeName)
                || "java.lang.Double".equals(typeName);
    }

    /**
     * Checks if it is a boolean type object.
     *
     * @param obj An object to check.
     * @return True if it is a boolean type object, false otherwise.
     */
    public static boolean isBool(Object obj) {
        return obj instanceof Boolean;
    }

    /**
     * Checks if it is a boolean type {@link Field}.
     *
     * @param field A field to check.
     * @return True if it is a boolean type field, false otherwise.
     */
    public static boolean isBool(Field field) {
        String typeName = field.getGenericType().getTypeName();
        return "boolean".equals(typeName) || "java.lang.Boolean".equals(typeName);
    }

    /**
     * Checks if it is a string type object.
     *
     * @param obj An object to check.
     * @return True if it is a string type object, false otherwise.
     */
    public static boolean isString(Object obj) {
        return (obj instanceof Character) || (obj instanceof String);
    }

    /**
     * Checks if it is a string type {@link Field}.
     *
     * @param field A field to check.
     * @return True if it is a string type field, false otherwise.
     */
    public static boolean isString(Field field) {
        String typeName = field.getGenericType().getTypeName();
        return "char".equals(typeName)
                || "java.lang.Character".equals(typeName)
                || "java.lang.String".equals(typeName);
    }

    /**
     * Checks if the object and field are the same loose type.
     *
     * @param object An object to check.
     * @param field A field to check.
     * @return True if the object and field are the same loose type, false otherwise.
     */
    public static boolean isSameLooseType(Object object, Field field) {
        return (isNumber(object) && isNumber(field))
                || (isReal(object) && isReal(field))
                || (isBool(object) && isBool(field))
                || (isString(object) && isString(field));
    }

    /**
     * Convert a value from string type.
     *
     * @param value The source string.
     * @param clazz Value class.
     * @param <T> Expected value type.
     * @return The value converted from string.
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromString(String value, Class<?> clazz) {
        if (clazz == Byte.class || clazz == byte.class) {
            return (T) Byte.valueOf(value);
        } else if (clazz == Short.class || clazz == short.class) {
            return (T) Short.valueOf(value);
        } else if (clazz == Integer.class || clazz == int.class) {
            return (T) Integer.valueOf(value);
        } else if (clazz == Long.class || clazz == long.class) {
            return (T) Long.valueOf(value);
        } else if (clazz == Float.class || clazz == float.class) {
            return (T) Float.valueOf(value);
        } else if (clazz == Double.class || clazz == double.class) {
            return (T) Double.valueOf(value);
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) (Boolean) (!StringUtils.isEmpty(value) && Boolean.parseBoolean(value));
        } else if (clazz == Character.class || clazz == char.class) {
            if (StringUtils.isNotEmpty(value)) {
                return (T) (Character) value.charAt(0);
            }
        } else if (clazz == String.class) {
            return (T) value;
        }
        return null;
    }
}
