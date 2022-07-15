package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DBIgnore {
    public String key() default "";
}