package eshioji.java8.twitter;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
import com.ning.http.client.*;
import com.ning.http.client.oauth.ConsumerKey;
import com.ning.http.client.oauth.OAuthSignatureCalculator;
import com.ning.http.client.oauth.RequestToken;
import eshioji.java8.common.ConcurrentUtils;
import eshioji.java8.common.VerboseRunnable;
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
public class TwitterStreamFactory {
    private static final Logger log = LoggerFactory.getLogger(TwitterStreamFactory.class);
    private static final String STREAM_BASE_URL = "https://stream.twitter.com/1.1/";

    private final int bufferSize;
    private final AsyncHttpClient httpClient;
    private final ExecutorService watcher;

    @Inject
    public TwitterStreamFactory(
            @Named("tw.consumer.key") String cKey,
            @Named("tw.consumer.secret") String cSecret,
            @Named("tw.request.token") String rToken,
            @Named("tw.request.secret") String rSecret,
            @Named("tw.stream.connection.timeout.ms") int connTimeout,
            @Named("tw.stream.request.timeout.ms") int reqTimeout,
            @Named("tw.stream.buffer.size") int bufferSize
    ){

        AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
                .setConnectionTimeoutInMs(connTimeout)
                .setRequestTimeoutInMs(reqTimeout)
                .setCompressionEnabled(true).build();

        AsyncHttpClient client = new AsyncHttpClient(config);
        ConsumerKey consumerKey = new ConsumerKey(cKey, cSecret);
        RequestToken token = new RequestToken(rToken, rSecret);
        OAuthSignatureCalculator calc = new OAuthSignatureCalculator(consumerKey, token);
        client.setSignatureCalculator(calc);

        this.httpClient = client;

        this.watcher = MoreExecutors.getExitingExecutorService(ConcurrentUtils.namedExecutor("watcher", 1, 1));

        this.bufferSize = bufferSize;

    }


    public Stream<byte[]> sample() throws IOException, ExecutionException, InterruptedException {
        String url = STREAM_BASE_URL + "statuses/sample.json";

        AsyncHttpClient.BoundRequestBuilder req =  httpClient.prepareGet(url);

        return setUpConnection(req, bufferSize);
    }

    public Stream<byte[]> filter(FilterQuery filterQuery) throws IOException, ExecutionException, InterruptedException {
        String url = STREAM_BASE_URL + "statuses/filter.json";

        AsyncHttpClient.BoundRequestBuilder req = httpClient
                .preparePost(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded");

        filterQuery.populate(req);

        return setUpConnection(req, bufferSize);
    }


    private Stream<byte[]> setUpConnection(AsyncHttpClient.BoundRequestBuilder req, int bufferSize) throws IOException {
        final BlockingQueue<byte[]> msg = new LinkedBlockingQueue<>(bufferSize);

        watcher.execute(new VerboseRunnable() {
            @Override
            public void doRun() throws Exception{
                while(!Thread.currentThread().isInterrupted()){
                    try {

                        req.execute(new AsyncCompletionHandler<Void>() {

                            @Override
                            public STATE onBodyPartReceived(HttpResponseBodyPart content) throws Exception {
                                msg.put(content.getBodyPartBytes());
                                return STATE.CONTINUE;
                            }

                            @Override
                            public Void onCompleted(Response response) throws Exception {
                                log.warn("Connection completed {}", response);
                                return null;
                            }

                        }).get();
                    }catch (ExecutionException e){
                        Throwable cause = Throwables.getRootCause(e);
                        log.warn("Connection lost due to "+cause+", reconnecting...", e);
                    }
                }
            }
        });

        return Stream.generate(() -> Uninterruptibles.takeUninterruptibly(msg));
    }
}
