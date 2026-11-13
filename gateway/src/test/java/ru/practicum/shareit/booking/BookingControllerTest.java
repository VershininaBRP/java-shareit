package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.BookingClient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createBooking_shouldReturnCreated() throws Exception {
        when(bookingClient.createBooking(anyLong(), any()))
                .thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "itemId": 10,
                                    "start": "2030-01-10T10:00:00",
                                    "end": "2030-01-11T10:00:00"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(bookingClient).createBooking(eq(1L), any());
    }

    @Test
    void approveBooking_shouldReturnOkWhenApproved() throws Exception {
        when(bookingClient.approveBooking(1L, 10L, true))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/10")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient).approveBooking(1L, 10L, true);
    }

    @Test
    void approveBooking_shouldReturnOkWhenRejected() throws Exception {
        when(bookingClient.approveBooking(1L, 10L, false))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/10")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "false"))
                .andExpect(status().isOk());

        verify(bookingClient).approveBooking(1L, 10L, false);
    }

    @Test
    void getBooking_shouldReturnOk() throws Exception {
        when(bookingClient.getBooking(1L, 10L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/10")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(bookingClient).getBooking(1L, 10L);
    }

    @Test
    void getBookingsByBooker_shouldUseAllStateByDefault() throws Exception {
        when(bookingClient.getBookingsByBooker(1L, "ALL"))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(bookingClient).getBookingsByBooker(1L, "ALL");
    }

    @Test
    void getBookingsByBooker_shouldPassState() throws Exception {
        when(bookingClient.getBookingsByBooker(1L, "WAITING"))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "WAITING"))
                .andExpect(status().isOk());

        verify(bookingClient).getBookingsByBooker(1L, "WAITING");
    }

    @Test
    void getBookingsByOwner_shouldUseAllStateByDefault() throws Exception {
        when(bookingClient.getBookingsByOwner(1L, "ALL"))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(bookingClient).getBookingsByOwner(1L, "ALL");
    }
}
