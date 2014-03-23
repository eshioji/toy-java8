package eshioji;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
* @author Enno Shioji (eshioji@gmail.com)
*/
public class EtlMain {
    private static final Logger log = LoggerFactory.getLogger(EtlMain.class);

    public static void main(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new Module());
        TestService testService = injector.getInstance(TestService.class);
        testService.awaitRunning();
    }
}
