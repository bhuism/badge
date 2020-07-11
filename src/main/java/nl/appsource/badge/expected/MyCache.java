package nl.appsource.badge.expected;

public interface MyCache<K, V> {

    V getIfPresent(K key);

    void put(K key, V value);

    int size();

}
