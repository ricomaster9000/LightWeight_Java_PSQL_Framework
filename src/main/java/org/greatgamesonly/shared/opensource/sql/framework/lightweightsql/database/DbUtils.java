package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database;


import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.*;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.RepositoryException;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.greatgamesonly.opensource.utils.reflectionutils.ReflectionUtils.*;
import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.errors.RepositoryError.REPOSITORY_INVALID_PARAM_NULL_VALUE__ERROR;
import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.errors.RepositoryError.REPOSITORY_INVALID_PARAM__ERROR;

public final class DbUtils {
    private DbUtils() {}
    private static final ConcurrentHashMap<Class<? extends BaseEntity>, HashMap<String,DbEntityColumnToFieldToGetter>> inMemoryDbEntityColumnToFieldToGetters = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, DbEntityColumnToFieldToGetter> inMemoryToOneReferenceFromDbEntityColumnToFieldToGetter = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<? extends BaseEntity>, List<DbEntityColumnToFieldToGetter>> inMemoryDbIgnoreFields = new ConcurrentHashMap<>();
    static final String NULL_STR = "NULL";
    static final String SINGLE_QUOTE_STR = "'";
    static final String EMPTY_STR = "";
    static final String ESCAPED_SINGLE_QUOTE_STR = "\\'";
    static final String PARENTHESIS_LEFT_STR = "(";
    static final String PARENTHESIS_RIGHT_STR = ")";

    public static Collection<DbEntityColumnToFieldToGetter> getDbEntityColumnToFieldToGetters(Class<? extends BaseEntity> entityClass) throws IntrospectionException {
        HashMap<String, DbEntityColumnToFieldToGetter> dbEntityColumnToFieldToGetters = inMemoryDbEntityColumnToFieldToGetters.get(entityClass);
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
                        .filter(dbEntityColumnToFieldToGetterOneToMany -> oneToManyAnnotation.referenceToColumnName().equals(dbEntityColumnToFieldToGetterOneToMany.getDbColumnName()))
                        .findFirst().orElseThrow(() -> new IntrospectionException("OneToMany annotation invalid, check following: make sure referenceToColumnName exists in referenced entity class, can only be applied to BaseEntity value types of list"))
                        .getGetterMethodName()
                    );
                    dbEntityColumnToFieldToGetter.setReferenceToColumnClassFieldSetterMethodName(getDbEntityColumnToFieldToGetters(oneToManyAnnotation.toManyEntityClass()).stream()
                        .filter(dbEntityColumnToFieldToGetterOneToMany -> oneToManyAnnotation.referenceToColumnName().equals(dbEntityColumnToFieldToGetterOneToMany.getDbColumnName()))
                        .findFirst().orElseThrow(() -> new IntrospectionException("OneToMany annotation invalid, check following: make sure referenceToColumnName exists in referenced entity class, can only be applied to BaseEntity value types of list"))
                        .getSetterMethodName()
                    );
                    dbEntityColumnToFieldToGetter.setAdditionalQueryToAdd(oneToManyAnnotation.addToWherePartInGetQuery());
                    dbEntityColumnToFieldToGetter.setInsertOrUpdateRelationInDbInteractions(true);
                    dbEntityColumnToFieldToGetter.setDeleteToManyEntitiesAutomaticallyOnDelete(oneToManyAnnotation.deleteToManyEntitiesAutomaticallyOnDelete());
                }
                if(field.isAnnotationPresent(OneToOne.class)) {
                    if(field.getType().getSuperclass() == null || !field.getType().getSuperclass().equals(BaseEntity.class)) {
                        throw new IntrospectionException("OneToOne annotation can only be applied to BaseEntity value type");
                    }
                    dbEntityColumnToFieldToGetter.setForOneToOneRelation(true);
                    dbEntityColumnToFieldToGetter.setLinkedClassEntity(field.getAnnotation(OneToOne.class).toOneEntityClass());
                    dbEntityColumnToFieldToGetter.setReferenceFromColumnName(field.getAnnotation(OneToOne.class).referenceFromColumnName());
                    dbEntityColumnToFieldToGetter.setReferenceToColumnName(field.getAnnotation(OneToOne.class).toOneEntityReferenceFromColumnName());

                    Collection<DbEntityColumnToFieldToGetter> toOneEntityDbEntityColumnToFieldsToGetters = getDbEntityColumnToFieldToGetters(field.getAnnotation(OneToOne.class).toOneEntityClass());

                    dbEntityColumnToFieldToGetter.setReferenceToColumnClassFieldGetterMethodName(toOneEntityDbEntityColumnToFieldsToGetters.stream()
                            .filter(dbEntityColumnToFieldToGetterOneToMany -> field.getAnnotation(OneToOne.class).toOneEntityReferenceFromColumnName().equals(dbEntityColumnToFieldToGetterOneToMany.getDbColumnName()))
                            .findFirst().orElseThrow(() -> new IntrospectionException(String.format("OneToOne relationship toOneEntityReferenceFromColumnName annotation field from %s must connect to a valid field in %s",entityClass,field.getType())))
                            .getGetterMethodName()
                    );
                    dbEntityColumnToFieldToGetter.setReferenceToColumnClassFieldSetterMethodName(toOneEntityDbEntityColumnToFieldsToGetters.stream()
                            .filter(dbEntityColumnToFieldToGetterOneToMany -> field.getAnnotation(OneToOne.class).toOneEntityReferenceFromColumnName().equals(dbEntityColumnToFieldToGetterOneToMany.getDbColumnName()))
                            .findFirst().orElseThrow(() -> new IntrospectionException(String.format("OneToOne relationship toOneEntityReferenceFromColumnName annotation field from %s must connect to a valid field in %s",entityClass,field.getType())))
                            .getSetterMethodName()
                    );
                    dbEntityColumnToFieldToGetter.setInsertOrUpdateRelationInDbInteractions(true);
                    dbEntityColumnToFieldToGetter.setToOneEntityReferenceFromColumnName(field.getAnnotation(OneToOne.class).toOneEntityReferenceFromColumnName());

                    if(toOneEntityDbEntityColumnToFieldsToGetters.stream().noneMatch(toOneEntityDbEntityColumnToFieldsToGetter -> toOneEntityDbEntityColumnToFieldsToGetter.getDbColumnName().equals(dbEntityColumnToFieldToGetter.getToOneEntityReferenceFromColumnName())))
                    {
                        throw new IntrospectionException(String.format("toOneEntityReferenceFromColumnName must link to a field in %s from %s for %s",field.getType(),entityClass,field.getName()));
                    }
                    if(Arrays.stream(fields).noneMatch(f -> f.isAnnotationPresent(OneToOneReferenceId.class) && f.getAnnotation(OneToOneReferenceId.class).columnName().equals(dbEntityColumnToFieldToGetter.getReferenceFromColumnName())))
                    {
                        throw new IntrospectionException(String.format("ReferenceFromColumnName %s must exist in %s",dbEntityColumnToFieldToGetter.getReferenceFromColumnName(),entityClass));
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
                if(field.isAnnotationPresent(OneToOneReferenceId.class)) {
                    dbEntityColumnToFieldToGetter.setForOneToOneReferenceId(true);
                    dbEntityColumnToFieldToGetter.setDbColumnName(field.getAnnotation(OneToOneReferenceId.class).columnName());
                    dbEntityColumnToFieldToGetter.setReferenceFromColumnName(field.getAnnotation(OneToOneReferenceId.class).columnName());
                    dbEntityColumnToFieldToGetter.setReferenceToColumnName(field.getAnnotation(OneToOneReferenceId.class).referenceToColumnName());
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
                    throw new IntrospectionException(String.format("annotation not set for db entity field %s in class %s, please set in code",field.getName(),entityClass));
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
            inMemoryDbEntityColumnToFieldToGetters.put(entityClass,dbEntityColumnToFieldToGetters);
        }
        return dbEntityColumnToFieldToGetters.values();
    }

    public static List<DbEntityColumnToFieldToGetter> getAllDbIgnoreGetterSetters(Class<? extends BaseEntity> entityClass) {
        List<DbEntityColumnToFieldToGetter> existingCachedDbIgnoreFields = inMemoryDbIgnoreFields.getOrDefault(entityClass, null);
        if(existingCachedDbIgnoreFields != null) {
            return existingCachedDbIgnoreFields;
        } else {
            existingCachedDbIgnoreFields = Stream.of(getClassFields(entityClass, false))
                    .filter(field -> field.isAnnotationPresent(DBIgnore.class))
                    .map(dbIgnoreField -> new DbEntityColumnToFieldToGetter(
                            "get"+capitalizeString(dbIgnoreField.getName()),
                            "set"+capitalizeString(dbIgnoreField.getName()),
                            new Class<?>[]{dbIgnoreField.getType()}))
                    .toList();
            inMemoryDbIgnoreFields.put(entityClass, existingCachedDbIgnoreFields);
            return existingCachedDbIgnoreFields;
        }
    }

    public static List<DbEntityColumnToFieldToGetter> getOneToManyRelationFieldToGetters(Class<? extends BaseEntity> entityClass) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass).stream()
                .filter(DbEntityColumnToFieldToGetter::isForOneToManyRelation)
                .toList();
    }

    public static List<DbEntityColumnToFieldToGetter> getOneToOneRelationFieldToGetters(Class<? extends BaseEntity> entityClass) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass).stream()
                .filter(DbEntityColumnToFieldToGetter::isForOneToOneRelation)
                .toList();
    }

    public static List<DbEntityColumnToFieldToGetter> getManyToOneRelationFieldToGetters(Class<? extends BaseEntity> entityClass) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass).stream()
                .filter(DbEntityColumnToFieldToGetter::isForManyToOneRelation)
                .toList();
    }

    public static List<DbEntityColumnToFieldToGetter> getAllRelationFieldToGetters(Class<? extends BaseEntity> entityClass) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass).stream()
                .filter(dbEntityColumnToFieldToGetter -> dbEntityColumnToFieldToGetter.isForOneToManyRelation() || dbEntityColumnToFieldToGetter.isForOneToOneRelation() || dbEntityColumnToFieldToGetter.isForManyToOneRelation())
                .toList();
    }

    public static DbEntityColumnToFieldToGetter getManyToOneRefIdRelationFieldToGetter(Class<? extends BaseEntity> entityClass, DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass).stream()
                .filter(dbEntityColumnToFieldToGet -> dbEntityColumnToFieldToGet.isForManyToOneReferenceId() && dbEntityColumnToFieldToGet.getReferenceFromColumnName().equals(dbEntityColumnToFieldToGetter.getLinkedDbColumnName()))
                .toList().get(0);
    }

    public static DbEntityColumnToFieldToGetter getOneToOneRefFromRelationColumnToFieldToGetter(Class<? extends BaseEntity> entityClass, DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter) throws IntrospectionException {
        DbEntityColumnToFieldToGetter result = inMemoryToOneReferenceFromDbEntityColumnToFieldToGetter.get(entityClass.toString()+dbEntityColumnToFieldToGetter.getReferenceFromColumnName());
        if(result == null) {
            result = getDbEntityColumnToFieldToGetters(entityClass).stream()
                    .filter(dbEntityColumnToFieldToGet -> dbEntityColumnToFieldToGet.isForOneToOneReferenceId() && dbEntityColumnToFieldToGet.getDbColumnName().equals(dbEntityColumnToFieldToGetter.getReferenceFromColumnName()))
                    .toList().get(0);
            inMemoryToOneReferenceFromDbEntityColumnToFieldToGetter.put(entityClass+dbEntityColumnToFieldToGetter.getReferenceFromColumnName(),result);
        }
        return result;
    }

    public static Map<String, String> getColumnsToFieldsMap(Class<? extends BaseEntity> entityClass) throws IntrospectionException {
        return getDbEntityColumnToFieldToGetters(entityClass)
                .stream()
                .filter(columnToField -> !columnToField.isForOneToManyRelation() && !columnToField.isForManyToOneRelation() && !columnToField.isForOneToOneRelation())
                .collect(Collectors.toMap(DbEntityColumnToFieldToGetter::getDbColumnName, DbEntityColumnToFieldToGetter::getClassFieldName));
    }
    public static String returnPreparedValueForQuery(Object object) {
        if(object == null) {
            return NULL_STR;
        }
        if(object instanceof String ||
           object instanceof java.util.Date ||
           object.getClass().isEnum() ||
           object instanceof Character ||
           object instanceof Calendar
        ) {
            return SINGLE_QUOTE_STR + object + SINGLE_QUOTE_STR;
        } else if(object instanceof BaseEntity) {
            return ((BaseEntity) object).getId().toString();
        } else {
            return object.toString();
        }
    }
    public static void validateSqlQueryParam(Object... sqlQueryParams) throws RepositoryException {
        validateSqlQueryParam(false, sqlQueryParams);
    }
    public static void validateSqlQueryParam(boolean minimalValidate, Object[] sqlQueryParams) throws RepositoryException {
        for(Object sqlQueryParam : sqlQueryParams) {
            // minimalValidate when set to true is mostly used to validate non-query-value params like columnNames and tableNames etc.
            if(!minimalValidate && sqlQueryParam == null) {
                throw new RepositoryException(REPOSITORY_INVALID_PARAM_NULL_VALUE__ERROR, sqlQueryParam);
            }
            if (sqlQueryParam instanceof String) {
                String paramStr = sqlQueryParam.toString();
                if(!minimalValidate) {
                    if(/*check for know SQL query parts that one almost never expects as a value in a query*/
                            Arrays.stream(sqlPsqlKnownQueryParts.values()).anyMatch(sqlPsqlKnownQueryPart -> paramStr.contains(sqlPsqlKnownQueryPart.toString()))
                    ) {
                        throw new RepositoryException(REPOSITORY_INVALID_PARAM__ERROR, paramStr);
                    }
                }
                /*check for subqueries*/
                if(paramStr.contains(PARENTHESIS_LEFT_STR) || paramStr.contains(PARENTHESIS_RIGHT_STR)) {
                    throw new RepositoryException(REPOSITORY_INVALID_PARAM__ERROR, paramStr);
                }

                // replace all escaped single quotes with nothing then check if non-escaped single quotes exist in query, throw excpetion if exists
                if(paramStr.replace(ESCAPED_SINGLE_QUOTE_STR, EMPTY_STR).contains(SINGLE_QUOTE_STR)) {
                    throw new RepositoryException(REPOSITORY_INVALID_PARAM__ERROR, paramStr);
                }
            }
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

    public static boolean isDbConnected(Connection con) {
        try {
            return con != null && !con.isClosed();
        } catch (SQLException ignored) {}
        return false;
    }

}
