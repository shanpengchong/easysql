package cn.easyutil.easySql.util;

import java.io.Serializable;
import java.util.function.Function;

public interface LambdaFunction<T, R> extends Function<T, R>, Serializable {

}
