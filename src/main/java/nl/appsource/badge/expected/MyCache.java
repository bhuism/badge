package nl.appsource.badge.expected;

interface MyCache<K, V> {

    V getIfPresent(K key);

    void put(K key, V value);

}
