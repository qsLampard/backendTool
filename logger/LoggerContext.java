
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Logs looks like 2016-12-09 17:03:10  INFO [New Thread3] (Use.java:54) - RequestId:Thread3 HostIP:10.10.10.10 HostName:qsLampard-lm ClientIp:11.11.11.11  [testMessage]

public class LoggerContext {

    private static String hostIP;
    private static String hostName;

    static {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            hostIP = ip.getHostAddress();
            hostName = ip.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    
    public static Logger getLogger(Class logClass) {
        MDC.put("HostIP", hostIP);
        MDC.put("HostName", hostName);
        return LoggerFactory.getLogger(logClass);
    }
    
    public static void setRequestId(String requestId) {
        MDC.put("RequestId", requestId);
    }

    public static void setClientIp(String clientIp) {
        MDC.put("ClientIP", clientIp);
    }
}
