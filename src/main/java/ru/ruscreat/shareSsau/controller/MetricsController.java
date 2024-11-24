package ru.ruscreat.shareSsau.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class MetricsController {

    @Autowired
    private MetricsEndpoint metricsEndpoint;

    private List<Map<String, Object>> history = new ArrayList<>();
    private final Queue<Long> requestTimestamps = new ConcurrentLinkedQueue<>();
    private final AtomicInteger currentSecondRequests = new AtomicInteger(0);
    private long currentSecond = System.currentTimeMillis() / 1000;

    @GetMapping("/api/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // Получаем текущее состояние
        double cpuUsage = metricsEndpoint.metric("system.cpu.usage", null).getMeasurements().get(0).getValue();
        double memoryUsageBytes = metricsEndpoint.metric("jvm.memory.used", null).getMeasurements().get(0).getValue();
        double memoryTotalBytes = metricsEndpoint.metric("jvm.memory.max", null).getMeasurements().get(0).getValue();

        // Конвертация в Мегабайты
        double memoryUsageMB = memoryUsageBytes / (1024 * 1024);
        double memoryTotalMB = memoryTotalBytes / (1024 * 1024);

        // Обновляем счетчик запросов в секунду
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        if (currentTimeSeconds > currentSecond) {
            currentSecond = currentTimeSeconds;
            currentSecondRequests.set(1); // Новая секунда, начинаем с 1 (текущий запрос)
        } else {
            currentSecondRequests.incrementAndGet(); // Увеличиваем счетчик для текущей секунды
        }

        // Добавляем в историю
        Map<String, Object> currentStats = new HashMap<>();
        currentStats.put("timestamp", System.currentTimeMillis());
        currentStats.put("cpuUsage", cpuUsage);
        currentStats.put("memoryUsage", memoryUsageMB);
        currentStats.put("memoryTotal", memoryTotalMB);
        currentStats.put("requestsPerSecond", currentSecondRequests.get());

        history.add(currentStats);

        // Оставляем только последние 60 записей
        if (history.size() > 60) {
            history.remove(0);
        }

        stats.put("current", currentStats);
        stats.put("history", history);
        return stats;
    }
}
