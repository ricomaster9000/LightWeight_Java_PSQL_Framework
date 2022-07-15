package org.greatgamesonly.shared.opensource.sql.framework.databasesetupmanager.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Entity {
    String key() default "";
    String tableName();

    Class<? extends BaseRepository<? extends BaseEntity>> repositoryClass();
}