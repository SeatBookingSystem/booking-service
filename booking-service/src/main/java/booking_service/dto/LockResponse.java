package booking_service.dto;

import lombok.Data;

@Data
public class LockResponse {

    private boolean isLocked;
    private int expiresInSeconds;
}
