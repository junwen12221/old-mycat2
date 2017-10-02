package io.mycat.mycat2.sqlparser.byteArrayInterface.dynamicAnnotation;

import io.mycat.mycat2.sqlannotations.SQLAnnotation;
import io.mycat.mycat2.sqlparser.BufferSQLContext;
import io.mycat.mycat2.sqlparser.byteArrayInterface.ByteArrayInterface;
import io.mycat.mycat2.sqlparser.byteArrayInterface.dynamicAnnotation.impl.ActonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by jamie on 2017/10/2.
 */
public class DynamicAnnotationMetrics {
    private static DynamicAnnotationMetrics ourInstance = new DynamicAnnotationMetrics();

    public static DynamicAnnotationMetrics getInstance() {
        return ourInstance;
    }

    private DynamicAnnotationMetrics() {
    }
    long threshold = 10000000;

    private static final Logger logger = LoggerFactory.getLogger(DynamicAnnotationMetrics.class);

    public String getActionConfigPath() {
        return ActonFactory.getInstance().getConfig();
    }

    public Map getActionMapInfo() {
        return (Map<String, Class<?>>) ActonFactory.getInstance().getResMap().clone();
    }

    public Map getTablesQueryCacheInfo() {
        return DynamicAnnotationManagerImpl.getInstance().cache;
    }

    public Map getMost() {
        return DynamicAnnotationManagerImpl.getInstance().cache;
    }

    public void record(long start, long end, BufferSQLContext context, List<SQLAnnotation> sqlAnnotations) {
        long interval;
        if ((interval = threshold + start) > end) {
            wirte(interval, context, sqlAnnotations);
        }
    }

    private void wirte(long interval, BufferSQLContext context, List<SQLAnnotation> sqlAnnotations) {
            ByteArrayInterface byteArrayInterface = context.getBuffer();
            logger.info(String.format("时间间隔:%d,sql:%s,actions:%s",
                    interval,
                    byteArrayInterface.getString(
                            byteArrayInterface.getOffset(),
                            byteArrayInterface.length()
                    ),
                    sqlAnnotations.stream().map((i)->i.getActionName()).collect(Collectors.joining("\n"))
            ));
    }


    static private class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private int cacheSize;

        public LRUCache(int cacheSize) {
            super(16, 0.75f, true);
            this.cacheSize = cacheSize;
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() >= cacheSize;
        }
    }


}
