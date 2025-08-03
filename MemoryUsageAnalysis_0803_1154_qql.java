// 代码生成时间: 2025-08-03 11:54:04
import io.vertx.core.Vertx;
import io.vertx.core.Verticle;
import io.vertx.core.Future;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MemoryUsageAnalysis is a Verticle that periodically logs the memory usage of the JVM.
 */
public class MemoryUsageAnalysis extends Verticle {

    private static final long DEFAULT_INTERVAL = 5; // Default interval in seconds
    private static final AtomicBoolean running = new AtomicBoolean(false);
    private long interval;
    private MemoryMXBean memoryMXBean;

    @Override
    public void start(Future<Void> startFuture) {
        interval = config().getLong("interval", DEFAULT_INTERVAL); // Get the interval from config
        memoryMXBean = ManagementFactory.getMemoryMXBean();

        // Schedule the memory usage check
        scheduleMemoryCheck();
        running.set(true);
        startFuture.complete();
    }

    @Override
    public void stop() throws Exception {
        running.set(false);
    }

    /**
     * Schedules the memory usage check with the given interval.
     */
    private void scheduleMemoryCheck() {
        if (running.get()) {
            vertx.setTimer(TimeUnit.SECONDS.toMillis(interval), id -> checkMemoryUsage());
        }
    }

    /**
     * Checks the memory usage and logs it.
     */
    private void checkMemoryUsage() {
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        String heapMemoryUsed = String.format("Heap Memory Used: %.2f MB", heapMemoryUsage.getUsed() / (1024.0 * 1024.0));
        String heapMemoryCommitted = String.format("Heap Memory Committed: %.2f MB", heapMemoryUsage.getCommitted() / (1024.0 * 1024.0));
        String nonHeapMemoryUsed = String.format("Non-Heap Memory Used: %.2f MB", nonHeapMemoryUsage.getUsed() / (1024.0 * 1024.0));
        String nonHeapMemoryCommitted = String.format("Non-Heap Memory Committed: %.2f MB", nonHeapMemoryUsage.getCommitted() / (1024.0 * 1024.0));

        System.out.println("Memory Usage Analysis: " + System.currentTimeMillis());
        System.out.println(heapMemoryUsed);
        System.out.println(heapMemoryCommitted);
        System.out.println(nonHeapMemoryUsed);
        System.out.println(nonHeapMemoryCommitted);

        // Schedule the next memory check
        scheduleMemoryCheck();
    }
}
