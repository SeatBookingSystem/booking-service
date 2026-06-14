package booking_service.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record UserId(@JsonValue String value) {

    @JsonCreator
    public static UserId from(String value) {
        if (value == null || !value.startsWith("USER:")) {
            throw new IllegalArgumentException("Invalid UserId");
        }
        return new UserId(value);
    }
}
