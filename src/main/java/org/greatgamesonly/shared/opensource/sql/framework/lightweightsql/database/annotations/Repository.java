package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.annotations;

import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.BaseEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Repository {
    public String key() default "";
    public Class<? extends BaseEntity> dbEntityClass();
    public int manyToOneCacheHours() default 0;
}