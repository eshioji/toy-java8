package eshioji.java8.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
public abstract class VerboseRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(VerboseRunnable.class);

    @Override
    public final void run() {
        try {
            doRun();
        } catch (Exception e) {
            log.error("Uncaught exception while executing " + this.getClass().getSimpleName(), e);
        } catch (Error e) {
            log.error("Uncaught error while executing " + this.getClass().getSimpleName(), e);
            throw e;
        } catch (Throwable e) {
            log.error("Uncaught throwable while executing " + this.getClass().getSimpleName(), e);
        }
    }

    protected abstract void doRun() throws Exception;
}
