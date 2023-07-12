package org.greatgamesonly.shared.opensource.sql.framework;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.ColumnName;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.PrimaryKey;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.Entity;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.BaseEntity;

@Entity(tableName = "status_type", repositoryClass = StatusTypeRepository.class)
public class StatusType extends BaseEntity {

    @PrimaryKey
    @ColumnName("id")
    private Long id;
    @ColumnName("name")
    private String name;

    public StatusType() {}

    public StatusType(String name) {
        this.name = name;
    }

    public StatusType(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
