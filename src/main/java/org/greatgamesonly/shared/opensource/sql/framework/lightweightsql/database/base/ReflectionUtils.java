package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.database.base;


import java.lang.reflect.InvocationTargetException;

class ReflectionUtils {

    // I WOULD SAY WITH THE LATER VERSIONS OF JAVA, JAVA Reflection logic should run fast enoug (if kept simple)
    protected static Object callReflectionMethod(Object object, String methodName, Object... methodParams) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object methodResult = null;
        if(methodParams == null || methodParams.length == 0) {
            methodResult = object.getClass().getMethod(methodName).invoke(object);
        } else {
            methodResult = object.getClass().getMethod(methodName).invoke(object, methodParams);
        }
        return methodResult;
    }
}
