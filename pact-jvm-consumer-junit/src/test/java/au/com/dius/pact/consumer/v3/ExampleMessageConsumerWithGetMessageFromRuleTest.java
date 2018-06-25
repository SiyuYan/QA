package au.com.dius.pact.consumer.v3;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.MessagePactProviderRule;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;


public class ExampleMessageConsumerWithGetMessageFromRuleTest {

    @Rule
    public MessagePactProviderRule messageProvider = new MessagePactProviderRule(this);

    @Pact(provider = "message_test_provider", consumer = "message_test_consumer")
    public MessagePact createPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();
        body.stringValue("testParam1", "value1");
        body.stringValue("testParam2", "value2");

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("contentType", "application/json");

        return builder.given("SomeProviderState")
                .expectsToReceive("a test message")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Test
    @PactVerification({"message_test_provider", "SomeProviderState"})
    public void test() throws Exception {
        assertNotNull(new String(messageProvider.getMessage()));
    }
}
