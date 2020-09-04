package nl.appsource.badge.cache;

import java.io.Serializable;
import java.util.function.Function;

public interface MyCache<V> {

    interface Stats {
        long getHits();

        long getMisses();

        long getSize();

        long getStarted();
    }

    V computeIfAbsent(final Serializable key, final Function<Serializable, V> valueSupplier);

    Stats getStats();

    void removeOldEntries();

}
