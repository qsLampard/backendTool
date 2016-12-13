
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class Use {
    
    //Sample of how to use Logger. Can be removed after Logger being widely used
    public static void main(String[] args) {
        Properties props = System.getProperties();
        props.setProperty("log4j.configuration", "file:src/test/resource/log4j.properties");
        Thread thread1 = new Thread("New Thread1") {
            public void run() {
                LoggerContext.setRequestId("USThread1");
                Logger logger = LoggerContext.getLogger(LoggerContext.class);
                while (true) {
                    try {
                        logger.debug("test");
                        Thread.sleep(50L);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };

         Thread thread2 = new Thread("New Thread2") {
             public void run() {
                 LoggerContext.setRequestId("Thread2");
                 Logger logger = LoggerContext.getLogger(LoggerContext.class);
                 
                 int i = 0;
                 Thread thread3 = new Thread("New Thread3") {
                     public void run() {
                         LoggerContext.setRequestId("Thread3");
                         Logger logger1 = LoggerContext.getLogger(LoggerContext.class);
                         while (true) {
                             try {
                                 logger1.info("info");
                                 Thread.sleep(60L);
                             } catch (InterruptedException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                             }
                         }
                     }
                 };
                 thread3.start();
                 while (true) {
                     try {
                         logger.error("error" + (i++));
                         Thread.sleep(60L);
                     } catch (InterruptedException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                 }
             }
        };
        thread1.start();
        thread2.start();
    }
}
