package com.oepfelbaum.transactionalcamel.routes;

import com.oepfelbaum.transactionalcamel.beans.ErrorRaiser;
import com.oepfelbaum.transactionalcamel.beans.OffsetProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

@Component
public class OmniRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .component("netty-http")
                .port(8000);

        errorHandler(
                deadLetterChannel("log:dead?level=ERROR")
                        .maximumRedeliveries(3)
        );

        // Kafka Consumer
        from("kafka:camel?" +
                "brokers=localhost:9094&" +
                "groupId=camelGroup" +
                "&maxPollRecords=3" +
                "&consumersCount=1" +
                "&autoOffsetReset=earliest" +
                "&autoCommitEnable=false" +
                "&allowManualCommit=true" +
                "&breakOnFirstError=true")
            .log("Consuming message from Kafka")
            // Copy Header to a Property, because the Header will be filtered out
            .setProperty(KafkaConstants.MANUAL_COMMIT, header(KafkaConstants.MANUAL_COMMIT))
            .choice()
                .when(body().contains("MQ"))
                    .log("Sending message to MQ")
                    .to("jms:queue:camel")
                .when(body().contains("REST"))
                    .log("Sending message to REST API")
                    .to("rest:get:api/receiver?host=localhost:8000")
                .otherwise()
                    .log("Intentionally produce error")
                    .to("direct:raiseError")
            .end()

            .onCompletion().onCompleteOnly()
                .process(new OffsetProcessor())
            .end();



        // IBM MQ Sink
        from("jms:queue:camel")
                .log("Message received from MQ");

        // REST API Sink
        rest("/api")
                .get("/receiver")
                .to("direct:log");
        from("direct:log")
                .log("Message received from REST");

        // Endpoint for raising errors
        from("direct:raiseError")
                .bean(ErrorRaiser.class)
                .to("jms:queue:camel");
    }
}
