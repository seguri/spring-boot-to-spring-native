package com.milleniumcare.precertification.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

@Component
public class CustomMetrics {

    private MeterRegistry meterRegistry;

    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        Gauge.builder("jvm_heap_memory_used", () -> ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / 1048576)
                .register(meterRegistry);
    }
}
