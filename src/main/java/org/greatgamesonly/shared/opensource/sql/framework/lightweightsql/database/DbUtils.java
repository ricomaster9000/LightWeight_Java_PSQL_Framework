package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database;


import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseEntity;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.greatgamesonly.reflection.utils.ReflectionUtils.*;

public class DbUtils {
    private static final Map<String, HashMap<String,DbEntityColumnToFieldToGetter>> inMemoryDbEntityColumnToFieldToGetters = new HashMap<>();

    public static Collection<DbEntityColumnToFieldToGetter> getDbEntityColumnToFieldToGetters(Class<?> entityClass) throws IntrospectionException {
        if(
            inMemoryDbEntityColumnToFieldToGetters.get(entityClass.getName()) == null ||
            inMemoryDbEntityColumnToFieldToGetters.get(entityClass.getName()).isEmpty()
        ) {
            boolean getSuperClassGettersAndSettersAlso = (entityClass.getSuperclass() != null &&
                    !entityClass.getSuperclass().equals(BaseEntity.class) &&
                    entityClass.getSuperclass().getSuperclass() != null &&
                    entityClass.getSuperclass().getSuperclass().equals(BaseEntity.class));

            inMemoryDbEntityColumnToFieldToGetters.put(entityClass.getName(), new HashMap<>());
            Field[] fields = getClassFields(entityClass, false, List.of(DBIgnore.class));
            Set<String> getters = getGetters(entityClass);
            Set<String> setters = getSetters(entityClass);

            if(getSuperClassGettersAndSettersAlso) {
                fields = concatenate(fields, getClassFields(entityClass.getSuperclass(),false,List.of(DBIgnore.class)));
                getters.addAll(getGetters(entityClass.getSuperclass()));
                setters.addAll(getSetters(entityClass.getSuperclass()));
            }

            for (Field field : fields) {
                boolean isValidField = (
                    BASE_VALUE_TYPES.contains(field.getType()) ||
                    Collection.class.isAssignableFrom(field.getType()) ||
                    BaseEntity.class.isAssignableFrom(field.getType()) ||
                    field.getType().isEnum() ||
                    field.getType().isPrimitive()
                );
                if(!isValidField) {
                    continue;
                }
                DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter = new DbEntityColumnToFieldToGetter();
                dbEntityColumnToFieldToGetter.setClassFieldName(field.getName());
                dbEntityColumnToFieldToGetter.setMethodParamTypes(field.getType());

                if(field.isAnnotationPresent(ModifyDateAutoSet.class)) {
                    dbEntityColumnToFieldToGetter.setModifyDateAutoSet(true);
                    dbEntityColumnToFieldToGetter.setModifyDateAutoSetTimezone(field.getAnnotation(ModifyDateAutoSet.class).timezone());
                }
                if(field.isAnnotationPresent(DoNotUpdateInDb.class)) {
                    dbEntityColumnToFieldToGetter.setCanBeUpdatedInDb(false);
                }
                if(field.isAnnotationPresent(OneToMany.class)) {
                    if(!field.getType().isInstance(List.class)) {
                        throw new IntrospectionException("OneToMany annotation can only be applied to BaseEntity value types of list");
                    }
                    dbEntityColumnToFieldToGetter.setForOneToManyRelation(true);
                    dbEntityColumnToFieldToGetter.setLinkedClassEntity(field.getAnnotation(OneToMany.class).toManyEntityClass());
                    dbEntityColumnToFieldToGetter.setReferenceToColumnName(field.getAnnotation(OneToMany.class).referenceToColumnName());
                    dbEntityColumnToFieldToGetter.setReferenceToColumnClassFieldGetterMethodName(getDbEntityColumnToFieldToGetters(field.getType()).stream()
                        .filter(dbEntityColumnToFieldToGetterOneToMany -> dbEntityColumnToFieldToGetterOneToMany.getDbColumnName().equals(field.getAnnotation(OneToMany.class).referenceToColumnName()))
                        .findFirst().orElseThrow(() -> new IntrospectionException("OneToMany annotation can only be applied to BaseEntity value types of list"))
                        .getGetterMethodName()
                    );
                    dbEntityColumnToFieldToGetter.setAdditionalQueryToAdd(field.getAnnotation(OneToMany.class).addToWherePartInGetQuery());
                    dbEntityColumnToFieldToGetter.setInsertOrUpdateRelationInDbInteractions(true);
                }
                if(field.isAnnotationPresent(OneToOne.class)) {
                    if(field.getType().getSuperclass() == null || !field.getType().getSuperclass().equals(BaseEntity.class)) {
                        throw new IntrospectionException("OneToMany annotation can only be applied to BaseEntity value type");
                    }
                    dbEntityColumnToFieldToGetter.setForOneToOneRelation(true);
                    dbEntityColumnToFieldToGetter.setLinkedClassEntity(field.getAnnotation(OneToOne.class).toOneEntityClass());
                    dbEntityColumnToFieldToGetter.setReferenceFromColumnName(field.getAnnotation(OneToOne.class).referenceFromColumnName());
                    dbEntityColumnToFieldToGetter.setReferenceToColumnName(field.getAnnotation(OneToOne.class).referenceToColumnName());
                    dbEntityColumnToFieldToGetter.setReferenceToColumnClassFieldGetterMethodName(getDbEntityColumnToFieldToGetters(field.getType()).stream()
                            .filter(dbEntityColumnToFieldToGetterOneToMany -> dbEntityColumnToFieldToGetterOneToMany.getDbColumnName().equals(field.getAnnotation(OneToOne.class).referenceToColumnName()))
                            .findFirst().orElseThrow(() -> new IntrospectionException(String.format("OneToOne relationship from %s must connect to a valid field in the %s",entityClass,field.getType())))
                            .getGetterMethodName()
                    );
                    dbEntityColumnToFieldToGetter.setInsertOrUpdateRelationInDbInteractions(true);
                }
                if(field.isAnnotationPresent(ManyToOne.class)) {
                    if(field.getType().getSuperclass() == null || !field.getType().getSuperclass().equals(BaseEntity.class)) {
                        throw new IntrospectionException("ManyToOne annotation can only be applied to BaseEntity value type");
                    }
                    dbEntityColumnToFieldToGetter.setForManyToOneRelation(true);
                    dbEntityColumnToFieldToGetter.setLinkedDbColumnName(field.getAnnotation(ManyToOne.class).linkedDbColumnName());
                    dbEntityColumnToFieldToGetter.setLinkedClassEntity(field.getAnnotation(ManyToOne.class).linkedEntityClass());
                    dbEntityColumnToFieldToGetter.setInsertOrUpdateRelationInDbInteractions(field.getAnnotation(ManyToOne.class).insertOrUpdateRelationInDbInteractions());
                }
                if(field.isAnnotationPresent(ManyToOneReferenceId.class)) {
                    dbEntityColumnToFieldToGetter.setForManyToOneReferenceId(true);
                    dbEntityColumnToFieldToGetter.setLinkedClassEntity(field.getAnnotation(ManyToOneReferenceId.class).toOneEntityClass());
                    dbEntityColumnToFieldToGetter.setDbColumnName(field.getAnnotation(ManyToOneReferenceId.class).referenceFromColumnName());
                    dbEntityColumnToFieldToGetter.setReferenceFromColumnName(field.getAnnotation(ManyToOneReferenceId.class).referenceFromColumnName());
                    dbEntityColumnToFieldToGetter.setReferenceToColumnName(field.getAnnotation(ManyToOneReferenceId.class).referenceToColumnName());
                }
                if(field.isAnnotationPresent(ColumnName.class)) {
                    dbEntityColumnToFieldToGetter.setDbColumnName(field.getAnnotation(ColumnName.class).value());
                }

                if(!field.isAnnotationPresent(ColumnName.class) &&
                   !field.isAnnotationPresent(OneToMany.class) &&
                   !field.isAnnotationPresent(OneToOne.class) &&
                   !field.isAnnotationPresent(ManyToOne.class) &&
                   !field.isAnnotationPresent(ManyToOneReferenceId.class) &&
                   (dbEntityColumnToFieldToGetter.getDbColumnName() == null || dbEntityColumnToFieldToGetter.getDbColumnName().isBlank())) {
                    throw new IntrospectionException("annotation not set for db entity field, please set in code");
                }
                if(setters.contains("set" + capitalizeString(dbEntityColumnToFieldToGetter.getClassFieldName()))) {
                    dbEntityColumnToFieldToGetter.setHasSetter(true);
                    dbEntityColumnToFieldToGetter.setSetterMethodName("set" + capitalizeString(dbEntityColumnToFieldToGetter.getClassFieldName()));
                }
                dbEntityColumnToFieldToGetter.setGetterMethodName(
                    getters.stream()
                        .filter(getter -> getter.equals("get" + capitalizeString(dbEntityColumnToFieldToGetter.getClassFieldName())))
                        .findFirst().orElse(null)
                );
                if(field.isAnnotationPresent(PrimaryKey.class)) {
                    dbEntityColumnToFieldToGetter.setIsPrimaryKey(true);
                    dbEntityColumnToFieldToGetter.setPrimaryKeyName(field.getName());
                }
                if(dbEntityColumnToFieldToGetter.getSetterMethodName() == null || dbEntityColumnToFieldToGetter.getGetterMethodName() == null) {
                    throw new IllegalArgumentException(
                            String.format("getter and setter methods could not be determined for field %s for class %s, please check if standard getter and setter methods exist for this field",
                                field.getName(),
                                entityClass
                            ));
                }
                inMemoryDbEntityColumnToFieldToGetters.get(entityClass.getName()).put(field.getName(),dbEntityColumnToFieldToGetter);
            }
        }
        return inMemoryDbEntityColumnToFieldToGetters.get(entityClass.getName()).values();
    }

    public static List<DbEntityColumnToFieldToGetter> getOneToManyRelationFieldToGetters(Class<?> entityClass) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass).stream()
                .filter(DbEntityColumnToFieldToGetter::isForOneToManyRelation)
                .collect(Collectors.toList());
    }

    public static List<DbEntityColumnToFieldToGetter> getOneToOneRelationFieldToGetters(Class<?> entityClass) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass).stream()
                .filter(DbEntityColumnToFieldToGetter::isForOneToOneRelation)
                .collect(Collectors.toList());
    }

    public static List<DbEntityColumnToFieldToGetter> getManyToOneRelationFieldToGetters(Class<?> entityClass) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass).stream()
                .filter(DbEntityColumnToFieldToGetter::isForManyToOneRelation)
                .collect(Collectors.toList());
    }

    public static List<DbEntityColumnToFieldToGetter> getAllRelationFieldToGetters(Class<?> entityClass) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass).stream()
                .filter(dbEntityColumnToFieldToGetter -> dbEntityColumnToFieldToGetter.isForOneToManyRelation() || dbEntityColumnToFieldToGetter.isForOneToOneRelation() || dbEntityColumnToFieldToGetter.isForManyToOneRelation())
                .collect(Collectors.toList());
    }

    public static DbEntityColumnToFieldToGetter getManyToOneRefIdRelationFieldToGetter(Class<?> entityClass, DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass).stream()
                .filter(dbEntityColumnToFieldToGet -> dbEntityColumnToFieldToGet.isForManyToOneReferenceId() && dbEntityColumnToFieldToGet.getReferenceFromColumnName().equals(dbEntityColumnToFieldToGetter.getLinkedDbColumnName()))
                .collect(Collectors.toList()).get(0);
    }

    public static Map<String, String> getColumnsToFieldsMap(Class<?> entityClass) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass)
                .stream()
                .filter(columnToField -> !columnToField.isForOneToManyRelation() && !columnToField.isForManyToOneRelation() && !columnToField.isForOneToOneRelation())
                .collect(Collectors.toMap(DbEntityColumnToFieldToGetter::getDbColumnName, DbEntityColumnToFieldToGetter::getClassFieldName));
    }

    public static String returnPreparedValueForQuery(Object object) {
        if(object instanceof String || object instanceof java.util.Date || object.getClass().isEnum()) {
            return "'"+ object +"'";
        } else {
            return object.toString();
        }
    }

    private static Calendar nowCal(String timezone) {
        return nowCal(timezone, 0);
    }

    private static Calendar nowCal(String timezone, int minusHours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        calendar.add(Calendar.HOUR, minusHours*-1);
        return calendar;
    }

    public static java.sql.Timestamp nowDbTimestamp() {
        return new java.sql.Timestamp(nowCal("UTC").getTimeInMillis());
    }

    public static java.sql.Timestamp nowDbTimestamp(int minusHours) {
        return new java.sql.Timestamp(nowCal("UTC", minusHours).getTimeInMillis());
    }

    public static java.sql.Timestamp nowDbTimestamp(String timezone) {
        return new java.sql.Timestamp(nowCal(timezone).getTimeInMillis());
    }

}
