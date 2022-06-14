package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.example;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.ColumnName;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.PrimaryKey;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.TableName;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseEntity;

@TableName("status_type")
public class StatusType extends BaseEntity {

    @PrimaryKey
    @ColumnName("id")
    private Long id;
    @ColumnName("name")
    private String name;

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
