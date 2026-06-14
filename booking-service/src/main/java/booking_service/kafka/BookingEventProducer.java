package booking_service.kafka;

import booking_service.event.BookingEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingEventProducer {

    private static final String TOPIC = "booking-events";

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    public BookingEventProducer(KafkaTemplate<String, BookingEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBookingEvent(BookingEvent event) {
        kafkaTemplate.send(TOPIC, event.getBookingId(), event);
    }
}
