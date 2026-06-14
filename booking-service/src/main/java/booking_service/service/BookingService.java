package booking_service.service;

import booking_service.client.SeatInfoClient;
import booking_service.dto.BookingResponse;
import booking_service.dto.LockResponse;
import booking_service.dto.Status;
import booking_service.event.BookingEvent;
import booking_service.kafka.BookingEventProducer;
import booking_service.record.SeatId;
import booking_service.record.ShowId;
import booking_service.record.UserId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class BookingService {

    private static final int LOCK_TTL = 120;

    private final StringRedisTemplate redisTemplate;
    private final SeatInfoClient seatInfoClient;
    private final BookingEventProducer bookingEventProducer;

    public BookingService(StringRedisTemplate redisTemplate, SeatInfoClient seatInfoClient, BookingEventProducer bookingEventProducer) {
        this.redisTemplate = redisTemplate;
        this.seatInfoClient = seatInfoClient;
        this.bookingEventProducer = bookingEventProducer;
    }

    public LockResponse lockSeat(ShowId showId, SeatId seatId, UserId userId) {

        Status status = seatInfoClient.getSeatStatus(showId.value(), seatId.value());

        if(!status.equals(Status.AVAILABLE)){
            LockResponse response = new LockResponse();
            response.setLocked(false);
            return response;
        }

        String key = buildKey(showId, seatId);

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, userId.value(), Duration.ofSeconds(LOCK_TTL));

        LockResponse response = new LockResponse();

        if (Boolean.TRUE.equals(success)) {
            response.setLocked(true);
            response.setExpiresInSeconds(LOCK_TTL);
        } else {
            response.setLocked(false);
        }

        return response;
    }


    public BookingResponse confirmSeatBooking(ShowId showId, SeatId seatId, UserId userId) {

        String key = buildKey(showId, seatId);

        String storedUser = redisTemplate.opsForValue().get(key);

        if (storedUser == null) {
            throw new RuntimeException("Lock expired");
        }

        if (!storedUser.equals(userId.value())) {
            throw new RuntimeException("Seat locked by another user");
        }

        Status status = seatInfoClient.getSeatStatus(showId.value(), seatId.value());

        if (!status.equals(Status.AVAILABLE)) {
            throw new RuntimeException("Seat already booked");
        }

        try {
            seatInfoClient.bookSeat(showId.value(), seatId.value());
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new RuntimeException("Booking failed, please retry");
        }

        String bookingId = "BOOK-" + System.currentTimeMillis();

        BookingResponse response = new BookingResponse();
        response.isBooked = true;
        response.bookingId = bookingId;
        response.message = "Booking successful";

        BookingEvent event = new BookingEvent();
        event.setBookingId(bookingId);
        event.setShowId(showId.value());
        event.setSeatId(seatId.value());
        event.setUserId(userId.value());

        bookingEventProducer.sendBookingEvent(event);

        return response;
    }

    public void unlockSeat(ShowId showId, SeatId seatId, UserId userId) {
        String key = buildKey(showId, seatId);

        String storedUser = redisTemplate.opsForValue().get(key);

        if (storedUser == null) {
            return;
        }

        if (!storedUser.equals(userId.value())) {
            throw new RuntimeException("Cannot unlock. Not your lock");
        }

        redisTemplate.delete(key);
    }

    private String buildKey(ShowId showId, SeatId seatId) {
        return "LOCK:" + showId.value() + ":" + seatId.value();
    }
}
