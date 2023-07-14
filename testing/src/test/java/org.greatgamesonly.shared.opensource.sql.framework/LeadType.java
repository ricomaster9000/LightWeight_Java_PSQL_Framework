package org.greatgamesonly.shared.opensource.sql.framework;


import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.BaseEntity;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.ColumnName;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.Entity;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations.PrimaryKey;

@Entity(tableName = "lead_type", repositoryClass = LeadTypeRepository.class)
public class LeadType extends BaseEntity {
    @PrimaryKey
    @ColumnName("id")
    protected Long id;
    @ColumnName("name")
    protected String name;

    public LeadType() {}

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
