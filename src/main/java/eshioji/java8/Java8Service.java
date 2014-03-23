package eshioji.java8;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


/**
* @author Enno Shioji (eshioji@gmail.com)
*/
public class Java8Service extends AbstractService{
    private static final Logger log = LoggerFactory.getLogger(Java8Service.class);


    public static void main(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new Java8Module());
        Java8Service java8Service = injector.getInstance(Java8Service.class);
        java8Service.awaitRunning();
    }

    @Inject
    public Java8Service(){

    }

    @Override
    protected void doStart() {
        notifyStarted();
    }

    @Override
    protected void doStop() {
        notifyStopped();
    }
}
