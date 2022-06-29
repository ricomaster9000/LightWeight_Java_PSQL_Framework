package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database;


import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseEntity;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class DbUtils {
    private static final Map<String, List<DbEntityColumnToFieldToGetter>> inMemoryDbEntityColumnToFieldToGetters = new HashMap<>();

    public static List<DbEntityColumnToFieldToGetter> getDbEntityColumnToFieldToGetters(Class<?> entityClass) throws IntrospectionException {
        if(
            inMemoryDbEntityColumnToFieldToGetters.get(entityClass.getName()) == null ||
            inMemoryDbEntityColumnToFieldToGetters.get(entityClass.getName()).isEmpty()
        ) {
            boolean getSuperClassGettersAndSettersAlso = (entityClass.getSuperclass() != null &&
                    !entityClass.getSuperclass().equals(BaseEntity.class) &&
                    entityClass.getSuperclass().getSuperclass() != null &&
                    entityClass.getSuperclass().getSuperclass().equals(BaseEntity.class));

            inMemoryDbEntityColumnToFieldToGetters.put(entityClass.getName(), new ArrayList<>());
            Field[] fields = getDbClassFields(entityClass);
            Set<String> getters = getGetters(entityClass);
            Set<String> setters = getSetters(entityClass);

            if(getSuperClassGettersAndSettersAlso) {
                fields = concatenate(fields, getDbClassFields(entityClass.getSuperclass()));
                getters.addAll(getGetters(entityClass.getSuperclass()));
                setters.addAll(getSetters(entityClass.getSuperclass()));
            }

            for (Field field : fields) {
                DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter = new DbEntityColumnToFieldToGetter();
                dbEntityColumnToFieldToGetter.setClassFieldName(field.getName());

                if(!field.isAnnotationPresent(ColumnName.class)) {
                    throw new IntrospectionException("ColumnName annotation not set for db entity field, please set in code");
                }
                if(field.isAnnotationPresent(ModifyDateAutoSet.class)) {
                    dbEntityColumnToFieldToGetter.setModifyDateAutoSet(true);
                    dbEntityColumnToFieldToGetter.setModifyDateAutoSetTimezone(field.getAnnotation(ModifyDateAutoSet.class).timezone());
                }
                dbEntityColumnToFieldToGetter.setDbColumnName(field.getAnnotation(ColumnName.class).value());

                dbEntityColumnToFieldToGetter.setHasSetter(setters.contains("set" + capitalizeString(dbEntityColumnToFieldToGetter.getClassFieldName())));
                dbEntityColumnToFieldToGetter.setGetterMethodName(
                    getters.stream()
                        .filter(getter -> getter.equals("get" + capitalizeString(dbEntityColumnToFieldToGetter.getClassFieldName())))
                        .findFirst().orElse(null)
                );
                if(field.isAnnotationPresent(PrimaryKey.class)) {
                    dbEntityColumnToFieldToGetter.setIsPrimaryKey(true);
                    dbEntityColumnToFieldToGetter.setPrimaryKeyName(field.getName());
                }
                inMemoryDbEntityColumnToFieldToGetters.get(entityClass.getName()).add(dbEntityColumnToFieldToGetter);
            }
        }
        return inMemoryDbEntityColumnToFieldToGetters.get(entityClass.getName());
    }

    public static Map<String, String> getColumnsToFieldsMap(Class<?> entityClass) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass)
                .stream()
                .collect(Collectors.toMap(DbEntityColumnToFieldToGetter::getDbColumnName, DbEntityColumnToFieldToGetter::getClassFieldName));
    }

    public static String returnPreparedValueForQuery(Object object) {
        if(object instanceof String || object instanceof java.util.Date || object.getClass().isEnum()) {
            return "'"+ object +"'";
        } else {
            return object.toString();
        }
    }

    private static Field[] getDbClassFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> (field.getType().equals(String.class) ||
                        field.getType().equals(Long.class) ||
                        field.getType().equals(Integer.class) ||
                        field.getType().equals(Date.class) ||
                        field.getType().equals(Boolean.class) ||
                        field.getType().isPrimitive() ||
                        field.getType().equals(Timestamp.class) ||
                        field.getType().isEnum()) &&
                        !field.isAnnotationPresent(DBIgnore.class)
                ).toArray(Field[]::new);
    }

    private static Set<String> getGetters(Class<?> clazz) throws IntrospectionException {
        return Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors())
                .filter(propertyDescriptor -> propertyDescriptor.getPropertyType().equals(String.class) ||
                        propertyDescriptor.getPropertyType().equals(Long.class) ||
                        propertyDescriptor.getPropertyType().equals(Integer.class) ||
                        propertyDescriptor.getPropertyType().equals(Date.class) ||
                        propertyDescriptor.getPropertyType().equals(Boolean.class) ||
                        propertyDescriptor.getPropertyType().isPrimitive() ||
                        propertyDescriptor.getPropertyType().equals(Timestamp.class) ||
                        propertyDescriptor.getPropertyType().isEnum()
                )
                .map(propertyDescriptor -> propertyDescriptor.getReadMethod().getName())
                .collect(Collectors.toSet());
    }

    private static Set<String> getSetters(Class<?> clazz) throws IntrospectionException {
        return Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors())
                .filter(propertyDescriptor -> propertyDescriptor .getWriteMethod() != null)
                .map(propertyDescriptor -> propertyDescriptor.getWriteMethod().getName())
                .collect(Collectors.toSet());
    }

    private static String capitalizeString(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    private static Calendar nowCal(String timezone) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        return calendar;
    }

    public static java.sql.Timestamp nowDbTimestamp(String timezone) {
        return new java.sql.Timestamp(nowCal(timezone).getTimeInMillis());
    }

}
