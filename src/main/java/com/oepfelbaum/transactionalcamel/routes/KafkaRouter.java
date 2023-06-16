package com.oepfelbaum.transactionalcamel.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.consumer.KafkaManualCommit;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.http.common.HttpMethods;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class KafkaRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        errorHandler(deadLetterChannel("log:dead?level=ERROR")
                .maximumRedeliveries(3)
        );

        // No Redelivery for 4xx errors
        onException(HttpOperationFailedException.class)
                .onWhen(exchange -> {
                    HttpOperationFailedException exception =
                            exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);

                    return HttpStatus.valueOf(exception.getStatusCode()).is4xxClientError();
                })
                .handled(true)
                .to("log:4xx?level=ERROR");

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
            // Copy Header to a Property, because the Header will be filtered
            .setProperty(KafkaConstants.MANUAL_COMMIT, header(KafkaConstants.MANUAL_COMMIT))
            .choice()
                .when(body().contains("MQ"))
                    .log("Sending message to MQ")
                    .to("jms:queue:sink")
                .when(body().contains("REST"))
                    .log("Sending message to REST API")
                    .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
                    .setHeader("Content-Type", constant("text/plain"))
                    .to("http://localhost:3000/api/messageSink")
            .end()

            // Commit Kafka Offset when Camel Exchange is completed
            .onCompletion().onCompleteOnly()
                .process(exchange -> {
                    Boolean lastOne = exchange.getIn().getHeader(KafkaConstants.LAST_RECORD_BEFORE_COMMIT, Boolean.class);

                    if (lastOne) {
                        KafkaManualCommit manual = exchange.getProperty(KafkaConstants.MANUAL_COMMIT, KafkaManualCommit.class);
                        if (manual != null) {
                            manual.commit();
                            System.out.println("Kafka offset committed");
                        }
                    }
                })
            .end();
    }
}
