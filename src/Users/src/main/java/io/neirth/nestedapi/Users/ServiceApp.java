/*
 * MIT License
 *
 * Copyright (c) 2020 NestedApi Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.neirth.nestedapi.Users;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import io.neirth.nestedapi.Users.Connectors.Connections;
import io.neirth.nestedapi.Users.Controllers.UsersRest;

public class ServiceApp {
    private static Logger loggerSystem = LogManager.getLogger(ServiceApp.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ServiceApp.class);

        /*
         * Here we load the connections from our database before it is
         * required by any client, since it is class during its installation
         * does many database and multithread related operations and takes
         * many seconds to complete.
         */
        app.addInitializers((context) -> {
            try {
                loggerSystem.info("Loading the database and broker connections...");
                Connections.getInstance();
            } catch (Exception e) {
                loggerSystem.error("The server couldn't be loaded, please check the debug info...");
                ServiceUtils.writeServerException(e);

                System.exit(-1);
            }
        });

        /*
         * Already after integrating the database initializers,
         * we start the spring boot application, starting at the beginning with these
         * initializers of the previous lines that will leave the connections ready
         * with the database.
         */
        app.run(args);
    }


    @Bean
    public ResourceConfig resourceConfig() {
        ResourceConfig config = new ResourceConfig();
        config.register(UsersRest.class);

        return config;
    }

    public static Logger getLoggerSystem() {
        return loggerSystem;
    }
}
