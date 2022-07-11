package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base.BaseEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToMany {
    public String key() default "";

    public String referenceToColumnName();

    public Class<? extends BaseEntity> toManyEntityClass();
}