package nl.appsource.badge.expected;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
public class MyCacheImpl<K, V> implements MyCache<K, V> {

    private final static long EXPIRED_IN_SECONDS = 60;

    private final static long STARTED = System.currentTimeMillis() / 1000;

    private long calls;
    private long misses;

    private final ConcurrentHashMap<K, Timed<V>> _cache = new ConcurrentHashMap<>();

    @Override
    public V computeIfAbsent(final K _key, final Function<? super K, ? extends V> valueSupplier) {
        removeOldEntries();
        calls++;
        return _cache.computeIfAbsent(_key, (key) -> {
            misses++;
            return new Timed<>(valueSupplier.apply(key), System.currentTimeMillis());
        }).getValue();
    }

    @Getter
    @RequiredArgsConstructor
    public class StatsImpl implements Stats {
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
