package io.neirth.nestedapi.Invoices;


import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.avro.generic.GenericRecord;

import io.neirth.nestedapi.Invoices.Templates.Invoice;
import org.jboss.logging.Logger;

public class ServiceUtils {
    private static Logger loggerSystem = Logger.getLogger(ServiceApp.class);
    
    public interface RestCallback {
        ResponseBuilder run() throws Exception;
    }

    public interface RpcCallback {
        Invoice run(GenericRecord consumedDatum) throws Exception;
    }

    /**
     * Method to write the exception in the logs.
     * 
     * It's useful for write the stack trace in debug mode or only the error in
     * production.
     * 
     * @param Exception The exception catched.
     */
    public static void writeServerException(Exception e) {
        if (ServiceUtils.getLoggerSystem().isDebugEnabled()) {
            // If the log level is set to debug, write the trace stack.
            ServiceUtils.getLoggerSystem().debug("An exception has occurred, getting the stacktrace of the exception: ");
            e.printStackTrace();
        } else {
            // Information for production log.
            ServiceUtils.getLoggerSystem().error("An exception has occurred, " + e.toString());
        }
    }

    /**
     * Method to access the event log of this module.
     * 
     * @return The logger instance.
     */
    public static Logger getLoggerSystem() {
        return loggerSystem;
    }

}