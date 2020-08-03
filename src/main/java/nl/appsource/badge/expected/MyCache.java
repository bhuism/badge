package nl.appsource.badge.expected;

import java.util.function.Function;

public interface MyCache<K, V> {

    interface Stats {
        long getHits();

        long getMisses();

        long getSize();

        long getStarted();
    }

    V computeIfAbsent(final K key, final Function<? super K, ? extends V> valueSupplier);

    Stats getStats();

    void removeOldEntries();

}
