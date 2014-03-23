package eshioji;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;


/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
public class TwitterStreamTest {
    private static final Logger log = LoggerFactory.getLogger(TwitterStreamTest.class);

    @Test
    public void testSample() throws Exception {
        Injector injector = Guice.createInjector(new Module());
        TwitterStream subject = injector.getInstance(TwitterStream.class);

        Stream<String> stream =  subject.sample();

        stream.forEach(System.out::println);

    }
}
