package io.neirth.nestedapi.Authentication;

import io.neirth.nestedapi.Authentication.Connectors.Connections;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class ServiceApp {

    /**
     * The main method used in Quarkus platform.
     */
    public static void main(String[] args) {
        Quarkus.run(MyApp.class, args);
    }

    /**
     * Static Class with init steps before publish the server.
     * 
     * This class initializes the connections with the broker and database,
     * for avoid the init during the clients request.
     */
    public static class MyApp implements QuarkusApplication {
        @Override
        public int run(String... args) {
            try {
                // Notify in the logs that the connections are being initialized.
                ServiceUtils.getLoggerSystem().info("Loading the connections...");

                // Initialize the instance of Connections, setting up the connections...
                Connections.getInstance().init();
                
                // Notify in the logs that the server is ready.
                ServiceUtils.getLoggerSystem().info("Ready for receive requests!");

                // Wait when quarkus app shutdowns the application.
                Quarkus.waitForExit();

                // Notify in the log that the connections are being closed.
                ServiceUtils.getLoggerSystem().info("Closing the connections...");

                // Close all external connections.
                Connections.getInstance().close();
            } catch(Exception e) {
                ServiceUtils.writeServerException(e);
            } 

            return 0;
        }
    }
}