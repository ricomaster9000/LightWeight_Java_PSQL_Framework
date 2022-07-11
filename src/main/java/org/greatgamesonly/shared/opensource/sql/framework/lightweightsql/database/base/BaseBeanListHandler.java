package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;


import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbEntityColumnToFieldToGetter;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.Entity;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.RepositoryException;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.greatgamesonly.reflection.utils.ReflectionUtils.callReflectionMethod;
import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.getManyToOneRefIdRelationFieldToGetter;
import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.getManyToOneRelationFieldToGetters;

public class BaseBeanListHandler<E extends BaseEntity> extends BeanListHandler<E> {

    private static final HashMap<Class<? extends BaseEntity>,List<BaseEntity>> manyToOneRelationValueHolder = new HashMap<>();
    private static final HashMap<Class<? extends BaseEntity>, Timestamp> manyToOneRelationCacheDateHolder = new HashMap<>();

    public BaseBeanListHandler(Class<? extends E> type) throws IntrospectionException, IOException, InterruptedException {
        super(type, new BasicRowProcessor(new BeanProcessor(DbUtils.getColumnsToFieldsMap(type))));
    }

    @Override
    public List<E> handle(ResultSet rs) throws SQLException {
        List<E> entities = super.handle(rs);
        for(E entity : entities) {
            try {
                List<DbEntityColumnToFieldToGetter> manyToOneRelationFieldToGetters = getManyToOneRelationFieldToGetters(entity.getClass());
                if(!manyToOneRelationFieldToGetters.isEmpty()) {
                    for (DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : manyToOneRelationFieldToGetters) {
                        final List<BaseEntity> toOneEntities = getManyToOneRelationValueHolder(dbEntityColumnToFieldToGetter.getLinkedClassEntity());
                        if(toOneEntities.isEmpty()) {
                            BaseRepository<? extends BaseEntity> toOneRepo = dbEntityColumnToFieldToGetter.getLinkedClassEntity().getAnnotation(Entity.class).repositoryClass().getDeclaredConstructor().newInstance();
                            toOneEntities.addAll(toOneRepo.getAll());
                            manyToOneRelationValueHolder.put(dbEntityColumnToFieldToGetter.getLinkedClassEntity(), toOneEntities);
                            manyToOneRelationCacheDateHolder.put(dbEntityColumnToFieldToGetter.getLinkedClassEntity(), DbUtils.nowDbTimestamp());
                        }
                        DbEntityColumnToFieldToGetter manyToOneRefIdRelationFieldToGetter = getManyToOneRefIdRelationFieldToGetter(entity.getClass(), dbEntityColumnToFieldToGetter);
                        Long entityManyToOneReferenceId = (Long) callReflectionMethod(entity,manyToOneRefIdRelationFieldToGetter.getGetterMethodName());
                        callReflectionMethod(
                                entity,
                                dbEntityColumnToFieldToGetter.getSetterMethodName(),
                                toOneEntities
                                        .stream()
                                        .filter(manyToOne -> manyToOne.getId().equals(entityManyToOneReferenceId))
                                        .findFirst()
                        );
                    }
                }
            } catch (RepositoryException | IntrospectionException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new SQLException("Unable to process entities: "+ e.getMessage(), e);
            }
        }
        return entities;
    }

    private List<BaseEntity> getManyToOneRelationValueHolder(Class<? extends BaseEntity> linkedClassEntity) {
        List<BaseEntity> result = manyToOneRelationValueHolder.get(linkedClassEntity);
        Timestamp timestamp = manyToOneRelationCacheDateHolder.get(linkedClassEntity);
        if(timestamp != null && timestamp.after(DbUtils.nowDbTimestamp(DbConnectionManager.getInMemoryCacheHoursForManyToOne()))) {
            manyToOneRelationValueHolder.remove(linkedClassEntity);
            result = new ArrayList<>();
        }
        if(result == null) {
            result = new ArrayList<>();
        }
        return result;
    }
}
