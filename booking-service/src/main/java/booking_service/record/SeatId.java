package booking_service.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record SeatId(@JsonValue String value) {

    @JsonCreator
    public static SeatId from(String value) {
        if (value == null || !value.startsWith("S")) {
            throw new IllegalArgumentException("Invalid SeatId");
        }
        return new SeatId(value);
    }
}
