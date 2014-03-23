package eshioji.java8.common;

import java.util.concurrent.*;


/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
public class ConcurrentUtils {
    private ConcurrentUtils(){}

    public static ThreadPoolExecutor namedExecutor(final String name, int thNum, int queueSize) {
        return new ThreadPoolExecutor(thNum, thNum, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueSize), r -> new Thread(r, name));
    }
}
