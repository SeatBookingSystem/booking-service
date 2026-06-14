package booking_service.event;

import lombok.Data;

@Data
public class BookingEvent {

    private String bookingId;
    private String showId;
    private String seatId;
    private String userId;

}
