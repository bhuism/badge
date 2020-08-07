package nl.appsource.badge.expected;

import java.io.Serializable;
import java.util.function.Function;

public interface MyCache<V> {

    interface Stats {
        long getHits();

        long getMisses();

        long getSize();

        long getStarted();
    }

    <L extends Serializable> V computeIfAbsent(final L key, final Function<L, V> valueSupplier);

    Stats getStats();

    void removeOldEntries();

}
