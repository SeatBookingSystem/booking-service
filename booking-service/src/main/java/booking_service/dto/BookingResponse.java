package booking_service.dto;

import lombok.Data;

@Data
public class BookingResponse {

    public boolean isBooked;
    public String bookingId;
    public String message;
}
