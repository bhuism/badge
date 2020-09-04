package nl.appsource.badge.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
public class MyCacheImpl<V> implements MyCache<V> {

    private final static long EXPIRED_IN_SECONDS = 60;

    private final static long STARTED = System.currentTimeMillis() / 1000;

    private long calls;
    private long misses;

    private final ConcurrentHashMap<Serializable, Timed<V>> _cache = new ConcurrentHashMap<>();

    @Override
    public V computeIfAbsent(Serializable _key, Function<Serializable, V> valueSupplier) {
        removeOldEntries();
        calls++;
        return _cache.computeIfAbsent(_key, (key) -> {
            misses++;
            return new Timed<>(valueSupplier.apply(key), System.currentTimeMillis());
        }).getValue();
    }

    @Getter
    @RequiredArgsConstructor
    public static class StatsImpl implements Stats {
        private final long hits;
        private final long misses;
        private final long size;
        private final long started;
    }

    @Override
    public Stats getStats() {
        removeOldEntries();
        return new StatsImpl(calls - misses, misses, _cache.mappingCount(), STARTED);
    }

    @Override
    public void removeOldEntries() {
        final Long expired = System.currentTimeMillis() - (EXPIRED_IN_SECONDS * 1000);

        _cache
            .entrySet()
            .removeIf(e -> e.getValue().getTimestamp() < expired);

    }
}
