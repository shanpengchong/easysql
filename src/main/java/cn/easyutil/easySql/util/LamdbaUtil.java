package cn.easyutil.easySql.util;
import cn.easyutil.easySql.annotations.TableField;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class LamdbaUtil {


    /**
     * 获取表达式方法中的字段名
     * @param fun   表达式
     * @return
     */
    public static <T> String getFieldName(LambdaFunction<T,?> fun){
        return getFieldName(fun,true);
    }

    /**
     * 获取表达式方法中的字段名
     * @param fun   表达式
     * @param conver    是否驼峰转下划线
     * @return
     */
    public static <T> String getFieldName(LambdaFunction<T,?> fun, boolean conver) {
        Field field = getField(fun);
        if(field == null){
            return null;
        }
        TableField an = field.getDeclaredAnnotation(TableField.class);
        if(an != null){
            if(an.value()!=null && an.value().length() > 0){
                return an.value();
            }
            if(an.name()!=null && an.name().length() > 0){
                return an.name();
            }
        }
        if(conver){
            return conversionMapUnderscore(field.getName());
        }
        return field.getName();
    }

    private static <T,R> Field getField(LambdaFunction<T,R> fun){
        SerializedLambda lambda = getSerializedLambda(fun);
        // 获取方法名
        String methodName = lambda.getImplMethodName();
        String fieldName = null;
        if(methodName.startsWith("get")){
            fieldName = methodName.substring(3);
        }else if(methodName.startsWith("is")){
            fieldName = methodName.substring(2);
        }
        if(fieldName == null){
            return null;
        }
        fieldName = fieldName.toUpperCase();
        String className = lambda.getImplClass().replace("/", ".");
        Field field = null;
        try {
            Class<?> aClass = Class.forName(className);
            Class clazz = aClass;
            while (clazz != Object.class){
                Field[] fields = aClass.getDeclaredFields();
                for (Field f : fields) {
                    if(f.getName().toUpperCase().equals(fieldName)){
                        return f;
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关键在于这个方法
     */
    public static SerializedLambda getSerializedLambda(Serializable fn) {
        // 提取SerializedLambda并缓存
        try {
            Method method = fn.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            return (SerializedLambda) method.invoke(fn);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 驼峰转下划线格式
     */
    public static  String conversionMapUnderscore(String v) {
        String str = "";
        for (int i = 0; i < v.toCharArray().length; i++) {
            char c = v.toCharArray()[i];
            if (i == 0) {
                c = Character.toLowerCase(c);
            } else if (Character.isUpperCase(c)) {
                str += "_" + Character.toLowerCase(c);
                continue;
            }
            str += c;
        }

        return str;
    }


}