package com.oepfelbaum.transactionalcamel.beans;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.consumer.KafkaManualCommit;

public class OffsetProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Boolean lastOne = exchange.getIn().getHeader(KafkaConstants.LAST_RECORD_BEFORE_COMMIT, Boolean.class);

        if (lastOne) {
            KafkaManualCommit manual = exchange.getProperty(KafkaConstants.MANUAL_COMMIT, KafkaManualCommit.class);
            if (manual != null) {
                manual.commit();
                System.out.println("Kafka offset committed");
            }
        }
    }
}
