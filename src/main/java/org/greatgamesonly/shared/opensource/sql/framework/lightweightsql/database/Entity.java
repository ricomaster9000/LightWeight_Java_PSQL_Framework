package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseEntity;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseRepository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
    public String key() default "";
    public String tableName();

    public Class<? extends BaseRepository<? extends BaseEntity>> repositoryClass();
}