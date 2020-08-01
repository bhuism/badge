package nl.appsource.badge.expected;

import lombok.extern.slf4j.Slf4j;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
public class MyCacheImpl<K, V> implements MyCache<K, V> {

    private final static long EXPIRED_IN_SECONDS = 15;

    private final ConcurrentHashMap<K, V> _cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<K, Long> _timestamp = new ConcurrentHashMap<>();

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> valueSupplier) {
        removeOldEntries();
        synchronized (this) {
            _timestamp.put(key, System.currentTimeMillis());
            return _cache.computeIfAbsent(key, valueSupplier);
        }
    }

    @Override
    public long mappingCount() {
        synchronized (this) {
            return _cache.mappingCount();
        }
    }

    private void removeOldEntries() {
        synchronized (this) {
            final Long expired = System.currentTimeMillis() - (EXPIRED_IN_SECONDS * 1000);
            _timestamp
                .entrySet()
                .stream()
                .filter(e -> e.getValue() < expired)
                .map(Entry::getKey)
                .forEach(_cache::remove);
        }
    }
}
