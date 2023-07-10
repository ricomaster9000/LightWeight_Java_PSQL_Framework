package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database;


import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.*;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.greatgamesonly.opensource.utils.reflectionutils.ReflectionUtils.*;

public class DbUtils {
    private static final Map<String, HashMap<String,DbEntityColumnToFieldToGetter>> inMemoryDbEntityColumnToFieldToGetters = new HashMap<>();
    private static final Map<String, DbEntityColumnToFieldToGetter> inMemoryToOneReferenceFromDbEntityColumnToFieldToGetter = new HashMap<>();

    public static Collection<DbEntityColumnToFieldToGetter> getDbEntityColumnToFieldToGetters(Class<?> entityClass) throws IntrospectionException {
        HashMap<String, DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = inMemoryDbEntityColumnToFieldToGetters.get(entityClass.getName());
        if(dbEntityColumnToFieldToGetters == null || dbEntityColumnToFieldToGetters.isEmpty()) {
            boolean getSuperClassGettersAndSettersAlso = (entityClass.getSuperclass() != null &&
                    !entityClass.getSuperclass().equals(BaseEntity.class) &&
                    entityClass.getSuperclass().getSuperclass() != null &&
                    entityClass.getSuperclass().getSuperclass().equals(BaseEntity.class));

            dbEntityColumnToFieldToGetters = new HashMap<>();
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
                    OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
                    if(!field.getType().isAssignableFrom(List.class)) {
                        throw new IntrospectionException(
                                String.format("OneToMany annotation can only be applied to BaseEntity value types of list, field = %s, entity-class = %s",field.getName(),entityClass.getSimpleName())
                        );
                    }
                    dbEntityColumnToFieldToGetter.setForOneToManyRelation(true);
                    dbEntityColumnToFieldToGetter.setLinkedClassEntity(oneToManyAnnotation.toManyEntityClass());
                    dbEntityColumnToFieldToGetter.setReferenceToColumnName(oneToManyAnnotation.referenceToColumnName());
                    dbEntityColumnToFieldToGetter.setReferenceToColumnClassFieldGetterMethodName(getDbEntityColumnToFieldToGetters(oneToManyAnnotation.toManyEntityClass()).stream()
                        .filter(dbEntityColumnToFieldToGetterOneToMany -> dbEntityColumnToFieldToGetterOneToMany.getDbColumnName().equals(oneToManyAnnotation.referenceToColumnName()))
                        .findFirst().orElseThrow(() -> new IntrospectionException("OneToMany annotation can only be applied to BaseEntity value types of list"))
                        .getGetterMethodName()
                    );
                    dbEntityColumnToFieldToGetter.setAdditionalQueryToAdd(oneToManyAnnotation.addToWherePartInGetQuery());
                    dbEntityColumnToFieldToGetter.setInsertOrUpdateRelationInDbInteractions(true);
                    dbEntityColumnToFieldToGetter.setDeleteToManyEntitiesAutomaticallyOnDelete(oneToManyAnnotation.deleteToManyEntitiesAutomaticallyOnDelete());
                }
                if(field.isAnnotationPresent(OneToOne.class)) {
                    if(field.getType().getSuperclass() == null || !field.getType().getSuperclass().equals(BaseEntity.class)) {
                        throw new IntrospectionException("OneToMany annotation can only be applied to BaseEntity value type");
                    }
                    dbEntityColumnToFieldToGetter.setForOneToOneRelation(true);
                    dbEntityColumnToFieldToGetter.setLinkedClassEntity(field.getAnnotation(OneToOne.class).toOneEntityClass());
                    dbEntityColumnToFieldToGetter.setReferenceFromColumnName(field.getAnnotation(OneToOne.class).referenceFromColumnName());

                    Collection<DbEntityColumnToFieldToGetter> toOneEntityDbEntityColumnToFieldsToGetters = getDbEntityColumnToFieldToGetters(field.getType());

                    dbEntityColumnToFieldToGetter.setReferenceToColumnClassFieldGetterMethodName(toOneEntityDbEntityColumnToFieldsToGetters.stream()
                            .filter(dbEntityColumnToFieldToGetterOneToMany -> dbEntityColumnToFieldToGetterOneToMany.getDbColumnName().equals(field.getAnnotation(OneToOne.class).toOneEntityReferenceFromColumnName()))
                            .findFirst().orElseThrow(() -> new IntrospectionException(String.format("OneToOne relationship toOneEntityReferenceFromColumnName annotation field from %s must connect to a valid field in %s",entityClass,field.getType())))
                            .getGetterMethodName()
                    );
                    dbEntityColumnToFieldToGetter.setInsertOrUpdateRelationInDbInteractions(true);
                    dbEntityColumnToFieldToGetter.setToOneEntityReferenceFromColumnName(field.getAnnotation(OneToOne.class).toOneEntityReferenceFromColumnName());

                    if(toOneEntityDbEntityColumnToFieldsToGetters.stream()
                        .noneMatch(toOneEntityDbEntityColumnToFieldsToGetter ->
                                toOneEntityDbEntityColumnToFieldsToGetter.getDbColumnName().equals(dbEntityColumnToFieldToGetter.getToOneEntityReferenceFromColumnName())
                        )
                    ) {
                        throw new IntrospectionException(String.format("ToOneEntityReferenceFromColumnName must link to a field in %s from %s for %s",field.getType(),entityClass,field.getName()));
                    }
                }
                if(field.isAnnotationPresent(ManyToOne.class)) {
                    if(field.getType().getSuperclass() == null || !field.getType().getSuperclass().equals(BaseEntity.class)) {
                        throw new IntrospectionException("ManyToOne annotation can only be applied to BaseEntity value type");
                    }
                    dbEntityColumnToFieldToGetter.setForManyToOneRelation(true);
                    dbEntityColumnToFieldToGetter.setLinkedDbColumnName(field.getAnnotation(ManyToOne.class).linkedDbColumnName());
                    dbEntityColumnToFieldToGetter.setLinkedClassEntity(field.getAnnotation(ManyToOne.class).toOneEntityClass());
                    dbEntityColumnToFieldToGetter.setInsertOrUpdateRelationInDbInteractions(field.getAnnotation(ManyToOne.class).insertOrUpdateRelationInDbInteractions());
                }
                if(field.isAnnotationPresent(ManyToOneReferenceId.class)) {
                    dbEntityColumnToFieldToGetter.setForManyToOneReferenceId(true);
                    dbEntityColumnToFieldToGetter.setDbColumnName(field.getAnnotation(ManyToOneReferenceId.class).columnName());
                    dbEntityColumnToFieldToGetter.setReferenceFromColumnName(field.getAnnotation(ManyToOneReferenceId.class).columnName());
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
                    throw new IntrospectionException(
                        String.format("getter and setter methods could not be determined for field %s for class %s, please check if standard getter and setter methods exist for this field",
                            field.getName(),
                            entityClass
                        )
                    );
                }
                dbEntityColumnToFieldToGetters.put(field.getName(),dbEntityColumnToFieldToGetter);
            }
            inMemoryDbEntityColumnToFieldToGetters.put(entityClass.getName(),dbEntityColumnToFieldToGetters);
        }
        return dbEntityColumnToFieldToGetters.values();
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

    public static DbEntityColumnToFieldToGetter getOneToOneRefFromRelationColumnToFieldToGetter(Class<?> entityClass, DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter) throws IntrospectionException {
        DbEntityColumnToFieldToGetter result = inMemoryToOneReferenceFromDbEntityColumnToFieldToGetter.get(entityClass.toString()+dbEntityColumnToFieldToGetter.getReferenceFromColumnName());
        if(result == null) {
            result = getDbEntityColumnToFieldToGetters(entityClass).stream()
                    .filter(dbEntityColumnToFieldToGet ->  dbEntityColumnToFieldToGet.getDbColumnName().equals(dbEntityColumnToFieldToGetter.getReferenceFromColumnName()))
                    .collect(Collectors.toList()).get(0);
            inMemoryToOneReferenceFromDbEntityColumnToFieldToGetter.put(entityClass.toString()+dbEntityColumnToFieldToGetter.getReferenceFromColumnName(),result);
        }
        return result;
    }

    public static Map<String, String> getColumnsToFieldsMap(Class<?> entityClass) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass)
                .stream()
                .filter(columnToField -> !columnToField.isForOneToManyRelation() && !columnToField.isForManyToOneRelation() && !columnToField.isForOneToOneRelation())
                .collect(Collectors.toMap(DbEntityColumnToFieldToGetter::getDbColumnName, DbEntityColumnToFieldToGetter::getClassFieldName));
    }

    public static String returnPreparedValueForQuery(Object object) {
        if(object == null) {
            return "NULL";
        }

        if(object instanceof String ||
           object instanceof java.util.Date ||
           object.getClass().isEnum() ||
           object instanceof Character ||
           object instanceof Calendar
        ) {
            return "'" + object + "'";
        } else if(object instanceof BaseEntity) {
            return ((BaseEntity) object).getId().toString();
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

    private static Calendar nowCalPlusMinutes(String timezone, int plusMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        calendar.add(Calendar.MINUTE, plusMinutes);
        return calendar;
    }

    public static java.sql.Timestamp nowDbTimestamp() {
        return new java.sql.Timestamp(nowCal("UTC").getTimeInMillis());
    }

    public static java.sql.Timestamp nowDbTimestamp(int minusHours) {
        return new java.sql.Timestamp(nowCal("UTC", minusHours).getTimeInMillis());
    }

    public static java.sql.Timestamp nowDbTimestampPlusMinutes(int plusMinutes) {
        return new java.sql.Timestamp(nowCalPlusMinutes("UTC", plusMinutes).getTimeInMillis());
    }

    public static java.sql.Timestamp nowDbTimestamp(String timezone) {
        return new java.sql.Timestamp(nowCal(timezone).getTimeInMillis());
    }

}
