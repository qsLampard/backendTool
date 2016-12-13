Simple Logging Facade for Java (SLF4J)

The Simple Logging Facade for Java (SLF4J) serves as a simple facade or abstraction for various logging frameworks (e.g. java.util.logging, logback, log4j) allowing the end user to plug in the desired logging framework at deployment time.

This implementation takes log4j for example. But Logback is native implementation of SLF4J and better than log4j.

This log statements follows pattern and generates standardized log messages with contextual attributes
Log format in key value pairs, with standard key names thereby making them splunkable.

For multiple threads, to keep these contextual attributes, we can use org.slf4j.MDC; It's like a Mapper which contains key-value pairs and for each thread it has its own MDC. If a thread is created by another thread, it will inherit the MDC and make copy of it.

Hints:
1. For log4j, set system property before using logger: props.setProperty("log4j.configuration", "file:src/test/resource/log4j.properties");
2. log4j.properties variables: %d time; %5p logger level; %F file name; %L line number; %X{MDC key} MDC value; %m logger message
