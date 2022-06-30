package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database;

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
}
