package io.mycat.mycat2.sqlparser.byteArrayInterface.dynamicAnnotation;

import java.util.function.Function;

/**
 * Created by jamie on 2017/10/2.
 */
public interface TimerProvider {
    long getTime();

    default long getTimeInterval(Runnable runnable) {
        long start = getTime();
        runnable.run();
        long end = getTime();
        return end - start;
    }


}
