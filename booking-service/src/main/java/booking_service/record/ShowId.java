package booking_service.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record ShowId(@JsonValue String value) {

    @JsonCreator
    public static ShowId from(String value) {
        if (value == null || !value.startsWith("SHOW")) {
            throw new IllegalArgumentException("Invalid ShowId");
        }
        return new ShowId(value);
    }
}
