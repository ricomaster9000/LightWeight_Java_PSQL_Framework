package org.greatgamesonly.shared.opensource.sql.framework.databasesetupmanager.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface ModifyDateAutoSet {
    String key() default "";

    String timezone() default "UTC";
}