package nl.appsource.badge.cache;

import lombok.Getter;

@Getter
public class Timed<V> {

    private final V value;
    private final Long timestamp;

    public Timed(final V value, Long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

}
