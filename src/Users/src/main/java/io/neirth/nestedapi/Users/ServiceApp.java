package io.neirth.nestedapi.Users;

import io.neirth.nestedapi.Users.Connectors.Connections;
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
        public int run(String... args) throws Exception {
            try {
                ServiceUtils.getLoggerSystem().info("Loading the connections...");
                Connections.getInstance().init();
                ServiceUtils.getLoggerSystem().info("Loaded the connections!!!");

                Quarkus.waitForExit();
            } catch(Exception e) {
                ServiceUtils.writeServerException(e);
            } 

            return 0;
        }
    }
}