package org.example.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

@Component
public class CpuHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // Get the operating system's MXBean
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        
        // Get the system load average or other CPU stats
        double cpuLoad = osBean.getSystemLoadAverage(); // System-wide load average for the last minute
        
        // For better details, you could add more metrics or calculations
        
        // Check if CPU load is above a threshold
        if (cpuLoad > 0.8) {  // For example, 80% load
            return Health.down().withDetail("CPU Load", cpuLoad).build();
        }
        
        return Health.up().withDetail("CPU Load", cpuLoad).build();
    }
}
