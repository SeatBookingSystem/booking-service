package booking_service;

import booking_service.client.SeatInfoClient;
import booking_service.dto.BookingResponse;
import booking_service.dto.LockResponse;
import booking_service.dto.Status;
import booking_service.record.SeatId;
import booking_service.record.ShowId;
import booking_service.record.UserId;
import booking_service.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private SeatInfoClient seatInfoClient;

	@InjectMocks
	private BookingService bookingService;

	private final ShowId showId = new ShowId("SHOW:1");
	private final SeatId seatId = new SeatId("SEAT:1");
	private final UserId userId = new UserId("USER:1");

	@BeforeEach
	void setup() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	void lockSeat_shouldLockSuccessfully() {
		when(seatInfoClient.getSeatStatus(showId.toString(), seatId.toString()))
				.thenReturn(Status.AVAILABLE);

		when(valueOperations.setIfAbsent(anyString(), anyString(), any()))
				.thenReturn(true);

		LockResponse response = bookingService.lockSeat(showId, seatId, userId);

		assertTrue(response.isLocked());
	}

	@Test
	void lockSeat_shouldFail_ifSeatNotAvailable() {
		when(seatInfoClient.getSeatStatus(showId.toString(), seatId.toString()))
				.thenReturn(Status.BOOKED);

		LockResponse response = bookingService.lockSeat(showId, seatId, userId);

		assertFalse(response.isLocked());
	}

	@Test
	void confirmBooking_shouldSucceed() {
		String key = "LOCK:SHOW:1:SEAT:1";

		when(valueOperations.get(key)).thenReturn("USER:1");
		when(seatInfoClient.getSeatStatus(showId.toString(), seatId.toString()))
				.thenReturn(Status.AVAILABLE);

		BookingResponse response = bookingService.confirmSeatBooking(showId, seatId, userId);

		assertTrue(response.isBooked);
		verify(redisTemplate).delete(key);
		verify(seatInfoClient).bookSeat(showId.toString(), seatId.toString());
	}

	@Test
	void confirmBooking_shouldFail_ifLockExpired() {
		String key = "LOCK:SHOW:1:SEAT:1";

		when(valueOperations.get(key)).thenReturn(null);

		assertThrows(RuntimeException.class, () ->
				bookingService.confirmSeatBooking(showId, seatId, userId)
		);
	}

	@Test
	void confirmBooking_shouldFail_ifDifferentUser() {
		String key = "LOCK:SHOW:1:SEAT:1";

		when(valueOperations.get(key)).thenReturn("USER:2");

		assertThrows(RuntimeException.class, () ->
				bookingService.confirmSeatBooking(showId, seatId, userId)
		);
	}

	@Test
	void unlockSeat_shouldDelete_ifSameUser() {
		String key = "LOCK:SHOW:1:SEAT:1";

		when(valueOperations.get(key)).thenReturn("USER:1");

		bookingService.unlockSeat(showId, seatId, userId);

		verify(redisTemplate).delete(key);
	}

	@Test
	void unlockSeat_shouldFail_ifDifferentUser() {
		String key = "LOCK:SHOW:1:SEAT:1";

		when(valueOperations.get(key)).thenReturn("USER:2");

		assertThrows(RuntimeException.class, () ->
				bookingService.unlockSeat(showId, seatId, userId)
		);
	}
}