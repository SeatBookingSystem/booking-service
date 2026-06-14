package booking_service.client;

import booking_service.dto.Status;
import booking_service.record.SeatId;
import booking_service.record.ShowId;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "seat-info-service", url = "http://localhost:8081")
public interface SeatInfoClient {

    @GetMapping("/v1/shows/{showId}/seats/{seatId}")
    Status getSeatStatus(@PathVariable String showId,
                         @PathVariable String seatId);

    @PutMapping("/v1/shows/{showId}/seats/{seatId}/book")
    void bookSeat(@PathVariable String showId,
                   @PathVariable String seatId);
}
