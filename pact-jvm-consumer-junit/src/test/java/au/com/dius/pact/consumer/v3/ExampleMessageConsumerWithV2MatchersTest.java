package au.com.dius.pact.consumer.v3;

import au.com.dius.pact.consumer.MatcherTestUtils;
import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.MessagePactProviderRule;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


public class ExampleMessageConsumerWithV2MatchersTest {

    @Rule
    public MessagePactProviderRule mockProvider = new MessagePactProviderRule(this);
    private byte[] currentMessage;

    @Pact(provider = "test_provider_v3", consumer = "test_consumer_v3")
    public MessagePact createPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody()
          .uuid("workflowId")
          .stringType("domain")
          .eachLike("values")
            .stringType("key")
            .stringType("value")
            .closeObject()
          .closeArray()
          .asBody();

        Map<String, String> metaData = new HashMap<>();
        metaData.put("contentType", "application/json");

      MessagePact messagePact = builder.given("executing a workflow with rabbitmq")
        .expectsToReceive("execution payload")
        .withContent(body)
        .withMetadata(metaData)
        .toPact();

      MatcherTestUtils.assertMessageMatcherKeysEqualTo(messagePact,
        "$.body.workflowId",
        "$.body.domain",
        "$.body.values",
        "$.body.values",
        "$.body.values[*].key",
        "$.body.values[*].value"
      );

      return messagePact;
    }

    @Test
    @PactVerification({"test_provider_v3", "executing a workflow with rabbitmq"})
    public void test() throws Exception {
        Assert.assertNotNull(new String(currentMessage));
    }

    public void setMessage(byte[] messageContents) {
        currentMessage = messageContents;
    }
}
