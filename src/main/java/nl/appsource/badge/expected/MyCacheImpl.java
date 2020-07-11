package nl.appsource.badge.expected;

import lombok.extern.slf4j.Slf4j;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MyCacheImpl<K, V> implements MyCache<K, V> {

    private final static long EXPIRED_IN_SECONDS = 15;

    private final ConcurrentHashMap<K, V> _cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<K, Long> _timestamp = new ConcurrentHashMap<>();

    @Override
    public V getIfPresent(final K key) {

        synchronized (this) {

            final Long expired = System.currentTimeMillis() - (EXPIRED_IN_SECONDS * 1000);

            _timestamp
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue() < expired)
                    .map(Entry::getKey)
                    .forEach(_cache::remove);
        }

        return _cache.get(key);
    }

    @Override
    public void put(final K key, final V value) {
        synchronized (this) {
            _cache.put(key, value);
            _timestamp.put(key, System.currentTimeMillis());
        }
    }

    @Override
    public int size() {
        synchronized (this) {
            return _cache.size();
        }
    }
}
