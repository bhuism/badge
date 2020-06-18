package nl.appsource.badge.expected;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MyCacheImpl<K, V> implements MyCache<K, V> {

    private final ConcurrentHashMap<K, V> _cache = new ConcurrentHashMap<>();

    public MyCacheImpl() {

        new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(60 * 1000);
                    _cache.clear();
                }
            } catch (final InterruptedException e) {
                log.error("", e);
            }
        }).start();

    }

    @Override
    public V getIfPresent(final K key) {
        return _cache.get(key);
    }

    @Override
    public void put(final K key, final V value) {
        _cache.put(key, value);
    }

}
