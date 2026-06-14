package booking_service.controller;

import booking_service.dto.BookingRequest;
import booking_service.dto.BookingResponse;
import booking_service.dto.LockResponse;
import booking_service.record.SeatId;
import booking_service.record.ShowId;
import booking_service.record.UserId;
import booking_service.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @CrossOrigin
    @PostMapping("/lock")
    public ResponseEntity<LockResponse> lockSeat(@RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(bookingService.lockSeat(bookingRequest.getShowId(), bookingRequest.getSeatId(), bookingRequest.getUserId()));
    }

    @CrossOrigin
    @PostMapping("/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(bookingService.confirmSeatBooking(bookingRequest.getShowId(), bookingRequest.getSeatId(), bookingRequest.getUserId()));
    }

    @DeleteMapping("/unlock")
    public ResponseEntity<Void> unlockSeat(
            @RequestParam ShowId showId,
            @RequestParam SeatId seatId,
            @RequestParam UserId userId
    ) {
        bookingService.unlockSeat(showId, seatId, userId);
        return ResponseEntity.noContent().build();
    }
}
