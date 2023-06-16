package com.oepfelbaum.transactionalcamel.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.http.common.HttpMethods;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class MQRouter extends RouteBuilder {

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
        from("jms:queue:camel?transacted=true")
                .choice()
                    .when(body().contains("MQ"))
                        .log("Sending message to MQ")
                        .to("jms:queue:sink")
                    .when(body().contains("REST"))
                        .log("Sending message to REST API")
                        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
                        .setHeader("Content-Type", constant("text/plain"))
                        .to("http://localhost:3000/api/messageSink")
                .end();
    }
}
