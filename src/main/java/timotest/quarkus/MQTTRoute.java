package timotest.quarkus;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.apache.camel.Exchange.*;
import static org.apache.camel.Exchange.HTTP_METHOD;

public class MQTTRoute extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MQTTRoute.class);

    @Override
    public void configure() throws Exception {
        from("paho:{{mqtt.config}}")
                .log("got message: ${body}")
                .process(in -> {
                    in.getIn().setHeader(CONTENT_TYPE, "application/vnd.kafka.json.v2+json");
                    String newBody = createKafkaMessage(in.getIn().getBody(String.class));
                    in.getIn().setBody(newBody);
                    in.getIn().setHeader(HTTP_METHOD, "POST");
                })
                .to("log:sender?showHeaders=true")
                .to("https:{{kafka.config}}");
    }

    private String createKafkaMessage(String body) {
        return "{\"records\": [  {\"value\":" + body + "} ]}";
    }
}