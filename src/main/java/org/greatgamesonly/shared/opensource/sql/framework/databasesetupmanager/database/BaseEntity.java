package org.greatgamesonly.shared.opensource.sql.framework.databasesetupmanager.database;

abstract class BaseEntity {
    protected abstract Long getId();
    protected abstract void setId(Long id);
}
