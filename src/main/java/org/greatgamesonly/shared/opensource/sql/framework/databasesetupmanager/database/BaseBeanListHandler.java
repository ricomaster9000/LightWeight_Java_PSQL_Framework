package org.greatgamesonly.shared.opensource.sql.framework.databasesetupmanager.database;


import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.beans.IntrospectionException;
import java.io.IOException;

class BaseBeanListHandler<E extends BaseEntity> extends BeanListHandler<E> {

    protected BaseBeanListHandler(Class<? extends E> type) throws IntrospectionException, IOException, InterruptedException {
        super(type, new BasicRowProcessor(new BeanProcessor(DbUtils.getColumnsToFieldsMap(type))));
    }
}
