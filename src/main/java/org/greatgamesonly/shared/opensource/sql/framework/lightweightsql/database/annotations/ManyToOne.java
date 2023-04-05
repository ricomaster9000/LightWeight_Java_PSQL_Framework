package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.BaseEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToOne {
    public String key() default "";

    public String linkedDbColumnName();

    public Class<? extends BaseEntity> toOneEntityClass();

    public boolean insertOrUpdateRelationInDbInteractions() default false;
}