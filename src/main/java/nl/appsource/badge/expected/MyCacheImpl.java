package nl.appsource.badge.expected;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
public class MyCacheImpl<K, V> implements MyCache<K, V> {

    private final static long EXPIRED_IN_SECONDS = 15;

    private final ConcurrentHashMap<K, Timed<V>> _cache = new ConcurrentHashMap<>();

    @Override
    public V computeIfAbsent(final K _key, final Function<? super K, ? extends V> valueSupplier) {
        removeOldEntries();
        return _cache.computeIfAbsent(_key, (key) -> new Timed<>(valueSupplier.apply(key), System.currentTimeMillis())).getValue();
    }

    @Override
    public long mappingCount() {
        return _cache.mappingCount();
    }

    private void removeOldEntries() {
        final Long expired = System.currentTimeMillis() - (EXPIRED_IN_SECONDS * 1000);

        _cache
            .entrySet()
            .removeIf(e -> e.getValue().getTimestamp() < expired);

    }
}
