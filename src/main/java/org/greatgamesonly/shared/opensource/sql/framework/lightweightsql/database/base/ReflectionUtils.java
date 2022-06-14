package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;


import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {

    protected static Object callReflectionMethod(Object object, String methodName) {
        Object methodResult = null;
        try {
            methodResult = object.getClass().getMethod(methodName).invoke(object);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return methodResult;
    }
}
