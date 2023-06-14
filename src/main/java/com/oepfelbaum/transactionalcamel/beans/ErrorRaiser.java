package com.oepfelbaum.transactionalcamel.beans;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;

public class ErrorRaiser {
    @Handler
    public void raiseError(Exchange exchange) throws  Exception {
        System.out.println("About to raise an error");
        throw new Exception("This is intended");
    }
}
