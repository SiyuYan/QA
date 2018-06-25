import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import com.amazonaws.HttpMethod;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpHeaders;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static junit.framework.TestCase.assertEquals;

public class pactWithAnnotationTest {

    private ObjectMapper mapper = new ObjectMapper();

    private static final String URL = "http://localhost:9002";
    private static final String path = "/cost/rest/analysis/services/statistic";

    private List<User> allUsers = newArrayList(
            new User("siyu", "yan", 18, "female"),
            new User("ranran", "liu", 28, "male"));

    private User expectedUser = new User("siyu", "yan", 18, "female");

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("service_provider", "localhost", 9002, this);

    @Pact(provider = "service_provider", consumer = "service_consumer")
    public PactFragment createFragment(PactDslWithProvider builder) throws JsonProcessingException {
        Map<String, String> headers = ImmutableMap.of(HttpHeaders.CONTENT_TYPE, "text/plain");

        return builder
                .given("there is a list a user data")
                .uponReceiving("expect receive all user data")
                .path(path)
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(mapper.writeValueAsString(allUsers))

                .given("there is a list a user data")
                .uponReceiving("expect receive specific user data according to name")
                .path(path + "/siyu")
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(mapper.writeValueAsString(expectedUser))
                .toFragment();
    }

    @PactVerification("service_provider")
    @Test
    public void runTest() throws Exception {
        //expect return all users when no params
        assertEquals(new ConsumerClient(URL).getAsString(path),
                mapper.writeValueAsString(allUsers));

        //expect return specific user by name
        assertEquals(new ConsumerClient(URL).getAsString(path + "/siyu"),
                mapper.writeValueAsString(expectedUser));
    }
}
