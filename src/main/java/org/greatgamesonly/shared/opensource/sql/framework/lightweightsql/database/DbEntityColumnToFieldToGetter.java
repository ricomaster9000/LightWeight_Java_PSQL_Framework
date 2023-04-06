package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database;

import java.util.Arrays;
import java.util.List;

public class DbEntityColumnToFieldToGetter {
    private String dbColumnName;
    private String classFieldName;
    private String getterMethodName;
    private String setterMethodName;
    private String primaryKeyName;
    private boolean hasSetter;
    private boolean isPrimaryKey;
    private boolean isModifyDateAutoSet;
    private String modifyDateAutoSetTimezone;
    private boolean canBeUpdatedInDb = true;

    private boolean isForOneToManyRelation = false;

    private boolean isForOneToOneRelation = false;

    private boolean isForManyToOneRelation = false;

    private boolean isForManyToOneReferenceId = false;

    private Class<? extends BaseEntity> linkedClassEntity = null;

    private String referenceFromColumnName;

    private String referenceToColumnName;

    private String toOneEntityReferenceFromColumnName;

    private String referenceToColumnClassFieldGetterMethodName;

    private String linkedDbColumnName;

    private String additionalQueryToAdd;

    private Class<?>[] methodParamTypes;

    private boolean insertOrUpdateRelationInDbInteractions = false;

    private boolean deleteToManyEntitiesAutomaticallyOnDelete = true;

    public String getDbColumnName() {
        return dbColumnName;
    }

    public void setDbColumnName(String dbColumnName) {
        this.dbColumnName = dbColumnName;
    }

    public String getClassFieldName() {
        return classFieldName;
    }

    public void setClassFieldName(String classFieldName) {
        this.classFieldName = classFieldName;
    }

    public String getGetterMethodName() {
        return getterMethodName;
    }

    public void setGetterMethodName(String getterMethodName) {
        this.getterMethodName = getterMethodName;
    }
    public String getSetterMethodName() {
        return setterMethodName;
    }
    public void setSetterMethodName(String setterMethodName) {
        this.setterMethodName = setterMethodName;
    }
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    public boolean hasSetter() {
        return hasSetter;
    }

    public void setHasSetter(boolean hasSetter) {
        this.hasSetter = hasSetter;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public boolean isModifyDateAutoSet() {
        return isModifyDateAutoSet;
    }

    public void setModifyDateAutoSet(boolean modifyDateAutoSet) {
        isModifyDateAutoSet = modifyDateAutoSet;
    }

    public String getModifyDateAutoSetTimezone() {
        return modifyDateAutoSetTimezone;
    }

    public void setModifyDateAutoSetTimezone(String modifyDateAutoSetTimezone) {
        this.modifyDateAutoSetTimezone = modifyDateAutoSetTimezone;
    }

    public boolean canBeUpdatedInDb() {
        return canBeUpdatedInDb;
    }

    public void setCanBeUpdatedInDb(boolean canBeUpdatedInDb) {
        this.canBeUpdatedInDb = canBeUpdatedInDb;
    }

    public boolean isForOneToManyRelation() {
        return isForOneToManyRelation;
    }

    public void setForOneToManyRelation(boolean forOneToManyRelation) {
        isForOneToManyRelation = forOneToManyRelation;
    }

    public Class<? extends BaseEntity> getLinkedClassEntity() {
        return linkedClassEntity;
    }

    public void setLinkedClassEntity(Class<? extends BaseEntity> linkedClassEntity) {
        this.linkedClassEntity = linkedClassEntity;
    }

    public String getReferenceFromColumnName() {
        return referenceFromColumnName;
    }

    public void setReferenceFromColumnName(String referenceFromColumnName) {
        this.referenceFromColumnName = referenceFromColumnName;
    }

    public String getReferenceToColumnName() {
        return referenceToColumnName;
    }

    public void setReferenceToColumnName(String referenceToColumnName) {
        this.referenceToColumnName = referenceToColumnName;
    }

    public String getReferenceToColumnClassFieldGetterMethodName() {
        return referenceToColumnClassFieldGetterMethodName;
    }

    public void setReferenceToColumnClassFieldGetterMethodName(String referenceToColumnClassFieldGetterMethodName) {
        this.referenceToColumnClassFieldGetterMethodName = referenceToColumnClassFieldGetterMethodName;
    }

    public boolean isForOneToOneRelation() {
        return isForOneToOneRelation;
    }

    public void setForOneToOneRelation(boolean forOneToOneRelation) {
        isForOneToOneRelation = forOneToOneRelation;
    }

    public boolean isForManyToOneRelation() {
        return isForManyToOneRelation;
    }

    public void setForManyToOneRelation(boolean forManyToOneRelation) {
        isForManyToOneRelation = forManyToOneRelation;
    }

    public boolean isForManyToOneReferenceId() {
        return isForManyToOneReferenceId;
    }

    public void setForManyToOneReferenceId(boolean forManyToOneReferenceId) {
        isForManyToOneReferenceId = forManyToOneReferenceId;
    }

    public String getLinkedDbColumnName() {
        return linkedDbColumnName;
    }

    public void setLinkedDbColumnName(String linkedDbColumnName) {
        this.linkedDbColumnName = linkedDbColumnName;
    }

    public String getAdditionalQueryToAdd() {
        return additionalQueryToAdd;
    }

    public void setAdditionalQueryToAdd(String additionalQueryToAdd) {
        this.additionalQueryToAdd = additionalQueryToAdd;
    }

    public Class<?>[] getMethodParamTypes() {
        return methodParamTypes;
    }

    public List<Class<?>> getMethodParamTypesAsList() {
        return Arrays.asList(methodParamTypes);
    }

    public void setMethodParamTypes(Class<?>... methodParamTypes) {
        this.methodParamTypes = methodParamTypes;
    }

    public boolean isInsertOrUpdateRelationInDbInteractions() {
        return insertOrUpdateRelationInDbInteractions;
    }

    public void setInsertOrUpdateRelationInDbInteractions(boolean insertOrUpdateRelationInDbInteractions) {
        this.insertOrUpdateRelationInDbInteractions = insertOrUpdateRelationInDbInteractions;
    }

    public String getToOneEntityReferenceFromColumnName() {
        return toOneEntityReferenceFromColumnName;
    }

    public void setToOneEntityReferenceFromColumnName(String toOneEntityReferenceFromColumnName) {
        this.toOneEntityReferenceFromColumnName = toOneEntityReferenceFromColumnName;
    }

    public boolean getDeleteToManyEntitiesAutomaticallyOnDelete() {
        return deleteToManyEntitiesAutomaticallyOnDelete;
    }

    public void setDeleteToManyEntitiesAutomaticallyOnDelete(boolean deleteToManyEntitiesAutomaticallyOnDelete) {
        this.deleteToManyEntitiesAutomaticallyOnDelete = deleteToManyEntitiesAutomaticallyOnDelete;
    }
}
