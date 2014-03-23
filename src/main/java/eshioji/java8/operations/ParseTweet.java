package eshioji.java8.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.function.Function;


/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
public class ParseTweet implements Function<byte[], ObjectNode> {
    private static final Logger log = LoggerFactory.getLogger(ParseTweet.class);
    private ObjectMapper om;

    @Inject
    public ParseTweet(ObjectMapper om){
        this.om = om;
    }

    @Override
    public ObjectNode apply(byte[] tweet) {
        try {
            return (ObjectNode) om.readTree(tweet);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
