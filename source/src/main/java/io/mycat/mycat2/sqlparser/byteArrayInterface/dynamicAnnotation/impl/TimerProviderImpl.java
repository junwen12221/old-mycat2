package io.mycat.mycat2.sqlparser.byteArrayInterface.dynamicAnnotation.impl;

import io.mycat.mycat2.sqlparser.byteArrayInterface.dynamicAnnotation.TimerProvider;

/**
 * Created by jamie on 2017/10/2.
 */
public class TimerProviderImpl implements TimerProvider{
    private static TimerProviderImpl ourInstance = new TimerProviderImpl();

    public static TimerProviderImpl getInstance() {
        return ourInstance;
    }

    private TimerProviderImpl() {
    }

    @Override
    public long getTime() {
        return System.nanoTime();
    }
}
