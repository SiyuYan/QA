package au.com.dius.pact.consumer.pactproviderrule;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.exampleclients.ConsumerClient;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.model.PactFragment;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PactMultiProviderTest {

    private static final String NAME_LARRY_JSON = "{\"name\": \"larry\"}";
    @Rule
    public PactProviderRule mockTestProvider = new PactProviderRule("test_provider", this);

    @Rule
    public PactProviderRule mockTestProvider2 = new PactProviderRule("test_provider2", this);

    @Pact(provider="test_provider", consumer="test_consumer")
    public PactFragment createFragment(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("testreqheader", "testreqheadervalue");

        return builder
            .given("good state")
            .uponReceiving("PactProviderTest test interaction")
                .path("/")
                .method("GET")
                .headers(headers)
            .willRespondWith()
                .status(200)
                .headers(headers)
                .body("{\"responsetest\": true, \"name\": \"harry\"}")
            .uponReceiving("PactProviderTest second test interaction")
                .method("OPTIONS")
                .headers(headers)
                .path("/second")
                .body("")
            .willRespondWith()
                .status(200)
                .headers(headers)
                .body("")
            .toFragment();
    }

    @Pact(provider="test_provider2", consumer="test_consumer")
    public PactFragment createFragment2(PactDslWithProvider builder) {
        return builder
                .given("good state")
                .uponReceiving("PactProviderTest test interaction")
                .path("/")
                .method("PUT")
                .body(NAME_LARRY_JSON)
                .willRespondWith()
                .status(200)
                .body("{\"responsetest\": true, \"name\": \"larry\"}")
                .toFragment();
    }

    @Test
    @PactVerification({"test_provider", "test_provider2"})
    public void allPass() throws IOException {
        doTest("/", NAME_LARRY_JSON);
    }

    @Test(expected = RuntimeException.class)
    @PactVerification({"test_provider", "test_provider2"})
    public void consumerTestFails() throws IOException, InterruptedException {
        doTest("/", NAME_LARRY_JSON);
        throw new RuntimeException("Oops");
    }

    @Test(expected = RuntimeException.class)
    @PactVerification(value = {"test_provider", "test_provider2"}, expectMismatch = true)
    public void provider1Fails() throws IOException, InterruptedException {
        doTest("/abc", NAME_LARRY_JSON);
    }

    @Test(expected = RuntimeException.class)
    @PactVerification(value = {"test_provider", "test_provider2"}, expectMismatch = true)
    public void provider2Fails() throws IOException, InterruptedException {
        doTest("/", "{\"name\": \"farry\"}");
    }

    @Test(expected = RuntimeException.class)
    @PactVerification(value = {"test_provider", "test_provider2"}, expectMismatch = true)
    public void bothprovidersFail() throws IOException, InterruptedException {
        doTest("/abc", "{\"name\": \"farry\"}");
    }

    private void doTest(String path, String json) throws IOException {
        Assert.assertEquals(new ConsumerClient(mockTestProvider.getConfig().url()).options("/second"), 200);
        Map expectedResponse = new HashMap();
        expectedResponse.put("responsetest", true);
        expectedResponse.put("name", "harry");
        assertEquals(new ConsumerClient(mockTestProvider.getConfig().url()).getAsMap(path, ""), expectedResponse);

        Map expectedResponse2 = new HashMap();
        expectedResponse2.put("responsetest", true);
        expectedResponse2.put("name", "larry");
        assertEquals(new ConsumerClient(mockTestProvider2.getConfig().url()).putAsMap("/", json),
                expectedResponse2);
    }
}
