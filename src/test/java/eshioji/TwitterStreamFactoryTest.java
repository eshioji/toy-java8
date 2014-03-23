package eshioji;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eshioji.java8.twitter.FilterQuery;
import eshioji.java8.Java8Module;
import eshioji.java8.twitter.TwitterStreamFactory;
import eshioji.java8.operations.ParseTweet;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;


/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
public class TwitterStreamFactoryTest {
    private static final Logger log = LoggerFactory.getLogger(TwitterStreamFactoryTest.class);

    @Test
    public void testSample() throws Exception {
        Injector injector = Guice.createInjector(new Java8Module());

        TwitterStreamFactory subject = injector.getInstance(TwitterStreamFactory.class);

        ParseTweet parseTweet = injector.getInstance(ParseTweet.class);


        Stream<byte[]> stream =  subject.sample();

//        stream.forEach(System.out::println);


        final AtomicLong counter = new AtomicLong();
        stream.parallel().map(parseTweet).forEach(parsed -> {
            if(counter.incrementAndGet() % 10 == 0){
                System.out.println(counter.get());
                System.out.println(parsed);
            }
        });

    }

    @Test
    public void testFilter() throws Exception {
        Injector injector = Guice.createInjector(new Java8Module());

        TwitterStreamFactory subject = injector.getInstance(TwitterStreamFactory.class);

        ParseTweet parseTweet = injector.getInstance(ParseTweet.class);


        FilterQuery fq = new FilterQuery();
        fq.language(new String[]{"en"});
        fq.track(new String[]{"http"});

        Stream<byte[]> stream =  subject.filter(fq);

        final AtomicLong counter = new AtomicLong();
        stream.parallel().map(parseTweet).forEach(parsed -> {
            if(counter.incrementAndGet() % 10 == 0){
                if(!parsed.get("text").asText().contains("http")){
                    throw new IllegalStateException(parsed.toString());
                }else{
                    log.info("OK");
                }
            }
        });

    }

    @Test
    public void scrap() throws Exception {
        Injector injector = Guice.createInjector(new Java8Module());
        TwitterStreamFactory subject = injector.getInstance(TwitterStreamFactory.class);

        ParseTweet parseTweet = injector.getInstance(ParseTweet.class);


        FilterQuery fq = new FilterQuery();
        fq.language(new String[]{"en"});
        fq.track(new String[]{"http"});

        Stream<byte[]> stream =  subject.filter(fq);

        final AtomicLong counter = new AtomicLong();
        stream.parallel().map(parseTweet).forEach(parsed -> {
            if(counter.incrementAndGet() % 10 == 0){
                if(!parsed.get("text").asText().contains("http")){
                    throw new IllegalStateException(parsed.toString());
                }else{
                    log.info("OK");
                }
            }
        });
    }

}
