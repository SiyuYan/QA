import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;


@Component
public class ConsumerClient {
    private String url;

    public ConsumerClient(String url) {
        this.url = url;
    }

    public String getAsString(String path) throws IOException, URISyntaxException {
        HttpGet request = new HttpGet(new URIBuilder(url).setPath(path).toString());
        HttpResponse response = HttpClientBuilder.create()
                .build().execute(request);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity()
                .getContent()));
        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
}