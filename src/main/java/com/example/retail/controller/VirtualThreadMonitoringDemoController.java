package com.example.retail.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * VT Demo Controller - handles direct /vt-demo/* endpoints called by UI
 */
@RestController
@RequestMapping("/vt-demo")
public class VirtualThreadMonitoringDemoController {

    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadMonitoringDemoController.class);

    @GetMapping("/burst")
    public String vtBurst(@RequestParam(defaultValue = "1000") int count) {
        logger.info("🚀 BURST TEST TRIGGERED - Creating {} virtual threads", count);

        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final int taskId = i;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(10 + (taskId % 50));
                    return "Task-" + taskId + " completed";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Task-" + taskId + " interrupted";
                }
            }, Executors.newVirtualThreadPerTaskExecutor());

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        logger.info("✅ BURST TEST COMPLETED - {} virtual threads finished", count);
        return String.format("Burst test completed: %d virtual threads at %s", count, LocalDateTime.now());
    }

    @GetMapping("/pinning")
    public String vtPinning(@RequestParam(defaultValue = "5") int count,      // Reduced to 5
                            @RequestParam(defaultValue = "50") int delayMs) {   // Reduced to 50ms
        logger.info("📌 PINNING TEST TRIGGERED - Creating {} threads with {}ms pinning", count, delayMs);

        final Object sharedLock = new Object();
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final int taskId = i;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    logger.debug("📌 Task {} entering synchronized block", taskId);

                    synchronized (sharedLock) {
                        logger.debug("📌 Task {} PINNED - sleeping for {}ms", taskId, delayMs);
                        Thread.sleep(delayMs); // This causes pinning!
                        return "Pinned-Task-" + taskId + " completed after " + delayMs + "ms";
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Pinned-Task-" + taskId + " interrupted";
                }
            }, Executors.newVirtualThreadPerTaskExecutor());

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        logger.info("✅ PINNING TEST COMPLETED - {} pinning events generated", count);
        return String.format("Pinning test completed: %d pinning events (%dms each) at %s", count, delayMs, LocalDateTime.now());
    }

    @GetMapping("/sustained")
    public String vtSustained(@RequestParam(defaultValue = "100") int count,
                              @RequestParam(defaultValue = "30") int durationSeconds) {
        logger.info("⏱️ SUSTAINED TEST TRIGGERED - {} VTs for {} seconds", count, durationSeconds);

        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final int taskId = i;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    for (int j = 0; j < durationSeconds; j++) {
                        Thread.sleep(1000);
                        if (j % 5 == 0) {
                            logger.debug("⏱️ Sustained task {} still running ({}s)", taskId, j);
                        }
                    }
                    return "Sustained-Task-" + taskId + " completed after " + durationSeconds + "s";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Sustained-Task-" + taskId + " interrupted";
                }
            }, Executors.newVirtualThreadPerTaskExecutor());

            futures.add(future);
        }

        // Return immediately, let threads run in background
        CompletableFuture.runAsync(() -> {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            logger.info("✅ SUSTAINED TEST COMPLETED - all {} tasks finished", count);
        });

        logger.info("✅ SUSTAINED TEST STARTED - {} VTs running for {}s", count, durationSeconds);
        return String.format("Sustained test started: %d VTs running for %ds at %s", count, durationSeconds, LocalDateTime.now());
    }
}