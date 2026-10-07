package org.springframework.boot.logging.log4j2;

import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.PropertySource;

/**
 * Boot 4's service descriptor points at this class, but its implementation calls
 * java.util.Map.forEach with the pre-2.15 Log4j BiConsumer. Keep that single
 * service provider compatible with the intentionally pinned Log4j API.
 */
public final class SpringBootPropertySource implements PropertySource {
    @Override
    public int getPriority() {
        return -200;
    }

    @Override
    public void forEach(BiConsumer<String, String> consumer) {
        consumer.accept("log4j.shutdownHookEnabled", "false");
    }

    @Override
    public CharSequence getNormalForm(Iterable<? extends CharSequence> tokens) {
        return "log4j2." + PropertySource.Util.joinAsCamelCase(tokens);
    }
}
