package ru.ruscreat.shareSsau.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class RateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, RequestTracker> requestMap;

    public RateLimiter(@Value("${rate.limiter.max-requests}") int maxRequests,
                      @Value("${rate.limiter.window-millis}") long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.requestMap = new ConcurrentHashMap<>();
    }

    public boolean isAllowed(String ip) {
        return requestMap.compute(ip, (key, tracker) -> {
            if (tracker == null) {
                tracker = new RequestTracker();
            }
            tracker.removeOldRequests();
            if (tracker.canAddRequest()) {
                tracker.addRequest();
                return tracker;
            }
            return tracker;
        }).canAddRequest();
    }

    private class RequestTracker {
        private final Queue<Long> requests = new ConcurrentLinkedQueue<>();

        public synchronized void removeOldRequests() {
            long currentTime = System.currentTimeMillis();
            while (!requests.isEmpty() && requests.peek() < currentTime - windowMillis) {
                requests.poll();
            }
        }

        public synchronized boolean canAddRequest() {
            return requests.size() < maxRequests;
        }

        public synchronized void addRequest() {
            requests.offer(System.currentTimeMillis());
        }
    }
}
