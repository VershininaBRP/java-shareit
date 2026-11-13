package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serialize_shouldNotIncludeItemId() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.of(2026, 11, 14, 10, 0));
        dto.setEnd(LocalDateTime.of(2026, 11, 15, 10, 0));
        dto.setItemId(10L);
        dto.setStatus(BookingStatus.WAITING);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).doesNotContain("itemId");
    }

    @Test
    void deserialize_shouldReadItemId() throws Exception {
        String json = "{" + """
        "id": 1,
        "start": "2026-11-14T10:00:00",
        "end": "2026-11-15T10:00:00",
        "itemId": 10,
        "status": "WAITING"}
        """;

        BookingDto dto = objectMapper.readValue(json, BookingDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getItemId()).isEqualTo(10L);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}
