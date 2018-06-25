import au.com.dius.pact.consumer.ConsumerPactTest;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpHeaders;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class pactTest extends ConsumerPactTest {
    private static final String path = "/cost/rest/analysis/services/statistic";

    private ArrayList<User> allUsers = newArrayList(
            new User("siyu", "yan", 18, "female"),
            new User("ranran", "liu", 28, "male"));
    private User expectedUser = new User("siyu", "yan", 18, "female");

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public PactFragment createFragment(PactDslWithProvider builder) {
        Map<String, String> headers = ImmutableMap.of(HttpHeaders.CONTENT_TYPE, "text/plain");
        try {
            return builder
                    .given("there is a list a user data")
                    .uponReceiving("expect receive all user data")
                    .path(path)
                    .method("GET")
                    .willRespondWith()
                    .status(200)
                    .headers(headers)
                    .body(mapper.writeValueAsString(allUsers))

                    .given("there is a list a user data")
                    .uponReceiving("expect receive specific user data according to name")
                    .path(path + "/siyu")
                    .method("GET")
                    .willRespondWith()
                    .status(200)
                    .headers(headers)
                    .body(mapper.writeValueAsString(expectedUser))
                    .toFragment();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected String providerName() {
        return "service_provider";
    }

    @Override
    protected String consumerName() {
        return "service_consumer";
    }

    @Override
    public void runTest(String url) throws IOException {
        try {
            //expect return all users when no params
            assertEquals(new ConsumerClient(url).getAsString(path),
                    mapper.writeValueAsString(allUsers));

            //expect return specific user by name
            assertEquals(new ConsumerClient(url).getAsString(path + "/siyu"),
                    mapper.writeValueAsString(expectedUser));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
