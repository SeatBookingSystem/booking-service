package booking_service.dto;

import booking_service.record.SeatId;
import booking_service.record.ShowId;
import booking_service.record.UserId;
import lombok.Getter;

@Getter
public class BookingRequest {

    private UserId userId;
    private ShowId showId;
    private SeatId seatId;
}
