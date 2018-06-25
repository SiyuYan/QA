package au.com.dius.pact.consumer;

import au.com.dius.pact.model.PactFragment;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class MatcherTestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherTestUtils.class);

    private MatcherTestUtils() {}

    public static Set<String> asSet(String... strings) {
        return new TreeSet<String>(Arrays.asList(strings));
    }

    public static void assertResponseMatcherKeysEqualTo(PactFragment fragment, String... matcherKeys) {
        Map<String, Map<String, Object>> matchers = fragment.interactions().head().getResponse().getMatchingRules();
        assertEquals(asSet(matcherKeys), new TreeSet<String>(matchers.keySet()));
    }

    public static void assertResponseKeysEqualTo(PactFragment fragment, String... keys) {
        String body = fragment.interactions().head().getResponse().getBody().getValue();
        Map hashMap = null;
        try {
            hashMap = new ObjectMapper().readValue(body, HashMap.class);
        } catch (IOException e) {
            LOGGER.error("Failed to parse JSON", e);
            Assert.fail(e.getMessage());
        }
        List<String> list = Arrays.asList(keys);
        Collections.sort(list);
        assertEquals(list, extractKeys(hashMap));
    }

    public static void assertMessageMatcherKeysEqualTo(MessagePact messagePact, String... matcherKeys) {
        Map<String, Map<String,Object>> matchers = messagePact.getMessages().get(0).getMatchingRules();
        assertEquals(asSet(matcherKeys), new TreeSet<String>(matchers.keySet()));
    }

    private static List<String> extractKeys(Map hashMap) {
        List<String> list = new ArrayList<String>();
        walkGraph(hashMap, list, "/");
        Collections.sort(list);
        return list;
    }

    private static void walkGraph(Map hashMap, List<String> list, String path) {
        for (Object o : hashMap.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            list.add(path + e.getKey());
            if (e.getValue() instanceof Map) {
                walkGraph((Map) e.getValue(), list, path + e.getKey() + "/");
            } else if (e.getValue() instanceof List) {
                walkList((List) e.getValue(), list, path + e.getKey() + "/");
            }
        }
    }

    private static void walkList(List value, List<String> list, String path) {
        for (int i = 0; i < value.size(); i++) {
            Object v = value.get(i);
            if (v instanceof Map) {
                walkGraph((Map) v, list, path + i + "/");
            } else if (v instanceof List) {
                walkList((List) v, list, path + i + "/");
            } else {
                list.add(path + v);
            }
        }
    }
}
