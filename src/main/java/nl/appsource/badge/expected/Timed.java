package nl.appsource.badge.expected;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class Timed<V> {

    private final V value;
    private final Long timestamp;

    public Timed(final V value, Long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

}
