package com.oepfelbaum.transactionalcamel.beans;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JmsConfig {

    @Bean
    public JmsComponent ibmmq() throws Exception {
        MQConnectionFactory cf = new MQConnectionFactory();
        cf.setQueueManager("QM1");
        cf.setChannel("SYSTEM.SSK.SVRCONN.1");
        cf.setConnectionNameList("localhost(1414)");

        JmsComponent jms = new JmsComponent();
        jms.setConnectionFactory(cf);
        return jms;
    }
}
