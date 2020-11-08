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
package io.neirth.nestedapi.invoices;

import io.neirth.nestedapi.invoices.connectors.Connections;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class ServiceApp 
{    
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
