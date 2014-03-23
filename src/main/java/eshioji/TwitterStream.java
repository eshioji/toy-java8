package eshioji;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
import com.ning.http.client.*;
import com.ning.http.client.oauth.ConsumerKey;
import com.ning.http.client.oauth.OAuthSignatureCalculator;
import com.ning.http.client.oauth.RequestToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;


/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
public class TwitterStream {
    private static final Logger log = LoggerFactory.getLogger(TwitterStream.class);
    private static final String STREAM_BASE_URL = "https://stream.twitter.com/1.1/";

    private final AsyncHttpClient httpClient;
    private final ExecutorService watcher;

    @Inject
    public TwitterStream(
            @Named("tw.consumer.key")String cKey,
            @Named("tw.consumer.secret")String cSecret,
            @Named("tw.request.token")String rToken,
            @Named("tw.request.secret")String rSecret
            ){
        AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
                .setConnectionTimeoutInMs(10000)
                .setRequestTimeoutInMs(10000)
                .setCompressionEnabled(true).build();

        AsyncHttpClient client = new AsyncHttpClient(config);
        ConsumerKey consumerKey = new ConsumerKey(cKey, cSecret);
        RequestToken token = new RequestToken(rToken, rSecret);
        OAuthSignatureCalculator calc = new OAuthSignatureCalculator(consumerKey, token);
        client.setSignatureCalculator(calc);

        this.httpClient = client;

        this.watcher = MoreExecutors.getExitingExecutorService(ConcurrentUtils.namedExecutor("watcher", 1, 1));

    }


    public Stream<String> sample() throws IOException, ExecutionException, InterruptedException {
        String url = STREAM_BASE_URL + "statuses/sample.json";

        final BlockingQueue<String> msg = setUpConnection(url);

        return Stream.generate(() -> Uninterruptibles.takeUninterruptibly(msg));

    }


    private BlockingQueue<String> setUpConnection(String url) throws IOException {
        final BlockingQueue<String> msg = new LinkedBlockingQueue<>(2000);

        watcher.execute(new VerboseRunnable() {
            @Override
            public void doRun() throws Exception{
                while(!Thread.currentThread().isInterrupted()){
                    try {

                        httpClient.prepareGet(url).execute(new AsyncCompletionHandler<Void>() {

                            @Override
                            public STATE onBodyPartReceived(HttpResponseBodyPart content) throws Exception {
                                String status = new String(content.getBodyPartBytes(), Charsets.UTF_8);
                                msg.put(status);
                                return STATE.CONTINUE;
                            }

                            @Override
                            public Void onCompleted(Response response) throws Exception {
                                log.debug("Connection completed {}", response);
                                return null;
                            }

                        }).get();
                    }catch (ExecutionException e){
                        Throwable cause = Throwables.getRootCause(e);
                        log.warn("Connection lost due to "+cause, e);
                    }
                }
            }
        });

        return msg;
    }
}
