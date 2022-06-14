package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;


import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils;

import java.beans.IntrospectionException;
import java.io.IOException;

public class BaseBeanListHandler<E> extends BeanListHandler<E> {

    public BaseBeanListHandler(Class<? extends E> type) throws IntrospectionException, IOException, InterruptedException {
        super(type, new BasicRowProcessor(new BeanProcessor(DbUtils.getColumnsToFieldsMap(type))));
    }
}
