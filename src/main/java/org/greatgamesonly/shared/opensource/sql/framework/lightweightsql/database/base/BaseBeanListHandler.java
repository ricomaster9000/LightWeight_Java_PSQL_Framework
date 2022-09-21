package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;


import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbEntityColumnToFieldToGetter;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.Entity;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.Repository;
import org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.RepositoryException;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import static org.greatgamesonly.opensource.utils.reflectionutils.ReflectionUtils.callReflectionMethodQuick;
import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.getManyToOneRefIdRelationFieldToGetter;
import static org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.DbUtils.getManyToOneRelationFieldToGetters;

public class BaseBeanListHandler<E extends BaseEntity> extends BeanListHandler<E> {

    private static final HashMap<Class<? extends BaseEntity>,HashMap<Long,Object>> manyToOneRelationValueHolder = new HashMap<>();
    private static final HashMap<Class<? extends BaseEntity>, Timestamp> manyToOneRelationCacheDateHolder = new HashMap<>();

    private final Repository repositoryAnnotation;

    public BaseBeanListHandler(Class<? extends E> type) throws IntrospectionException, IOException, InterruptedException {
        super(type, new BasicRowProcessor(new BeanProcessor(DbUtils.getColumnsToFieldsMap(type))));
        repositoryAnnotation = type.getAnnotation(Entity.class).repositoryClass().getAnnotation(Repository.class);
    }

    @Override
    public List<E> handle(ResultSet rs) throws SQLException {
        List<E> entities = super.handle(rs);
        for(E entity : entities) {
            try {
                List<DbEntityColumnToFieldToGetter> manyToOneRelationFieldToGetters = getManyToOneRelationFieldToGetters(entity.getClass());
                if (!manyToOneRelationFieldToGetters.isEmpty()) {
                    for (DbEntityColumnToFieldToGetter dbEntityColumnToFieldToGetter : manyToOneRelationFieldToGetters) {
                        final HashMap<Long, Object> toOneEntities = getManyToOneRelationValueHolder(dbEntityColumnToFieldToGetter.getLinkedClassEntity());
                        if (toOneEntities.isEmpty()) {
                            getAndSetToOneEntities(dbEntityColumnToFieldToGetter.getLinkedClassEntity(), toOneEntities);
                        }
                        DbEntityColumnToFieldToGetter manyToOneRefIdRelationFieldToGetter = getManyToOneRefIdRelationFieldToGetter(entity.getClass(), dbEntityColumnToFieldToGetter);
                        Object entityManyToOneReferenceIdVal = callReflectionMethodQuick(entity, manyToOneRefIdRelationFieldToGetter.getGetterMethodName());
                        if (entityManyToOneReferenceIdVal == null) {
                            continue;
                        }
                        try {
                            callReflectionMethodQuick(entity, dbEntityColumnToFieldToGetter.getSetterMethodName(), toOneEntities.get((Long) entityManyToOneReferenceIdVal), dbEntityColumnToFieldToGetter.getMethodParamTypes()[0]);
                        } catch (IllegalArgumentException e) {
                            getAndSetToOneEntities(dbEntityColumnToFieldToGetter.getLinkedClassEntity(),toOneEntities);
                            callReflectionMethodQuick(entity, dbEntityColumnToFieldToGetter.getSetterMethodName(), toOneEntities.get((Long) entityManyToOneReferenceIdVal), dbEntityColumnToFieldToGetter.getMethodParamTypes()[0]);
                        }
                    }
                }
            } catch (RepositoryException | IntrospectionException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new SQLException("Unable to process entities: "+ e.getMessage(), e);
            }
        }
        return entities;
    }

    private void getAndSetToOneEntities(Class<? extends BaseEntity> linkedClassEntity, final HashMap<Long,Object> toOneEntities) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, RepositoryException {
        BaseRepository<? extends BaseEntity> toOneRepo = linkedClassEntity.getAnnotation(Entity.class).repositoryClass().getDeclaredConstructor().newInstance();
        toOneRepo.getAll().forEach(toOneEntity -> toOneEntities.put(toOneEntity.getId(),toOneEntity));
        BaseBeanListHandler.manyToOneRelationValueHolder.put(linkedClassEntity, toOneEntities);
        BaseBeanListHandler.manyToOneRelationCacheDateHolder.put(linkedClassEntity, DbUtils.nowDbTimestamp());
    }

    private HashMap<Long,Object> getManyToOneRelationValueHolder(Class<? extends BaseEntity> linkedClassEntity) {
        HashMap<Long,Object> result = BaseBeanListHandler.manyToOneRelationValueHolder.get(linkedClassEntity);
        Timestamp timestamp = BaseBeanListHandler.manyToOneRelationCacheDateHolder.get(linkedClassEntity);
        if(timestamp != null && timestamp.before(DbUtils.nowDbTimestamp(repositoryAnnotation.manyToOneCacheHours()))) {
            BaseBeanListHandler.manyToOneRelationValueHolder.remove(linkedClassEntity);
            result = new HashMap<>();
        }
        if(result == null) {
            result = new HashMap<>();
        }
        return result;
    }
}
