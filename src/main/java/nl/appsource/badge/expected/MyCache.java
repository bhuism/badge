package nl.appsource.badge.expected;

import java.util.function.Function;

public interface MyCache<K, V> {

    V computeIfAbsent(final K key, final Function<? super K, ? extends V> valueSupplier);

    long mappingCount();

}
