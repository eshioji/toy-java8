package eshioji;

import com.google.common.util.concurrent.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


/**
* @author Enno Shioji (eshioji@gmail.com)
*/
public class TestService extends AbstractService{
    private static final Logger log = LoggerFactory.getLogger(TestService.class);



    @Inject
    public TestService(){

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
