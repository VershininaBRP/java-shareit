package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void create_shouldReturnCreatedBooking() throws Exception {
        BookingDto input = new BookingDto();
        input.setStart(LocalDateTime.now().plusDays(1));
        input.setEnd(LocalDateTime.now().plusDays(2));
        input.setItemId(10L);

        Booking booking = createBooking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        when(bookingService.create(eq(1L), any(Booking.class)))
                .thenReturn(booking);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.item.id").value(10))
                .andExpect(jsonPath("$.item.name").value("Дрель"))
                .andExpect(jsonPath("$.booker.id").value(2))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).create(eq(1L), any(Booking.class));
    }

    @Test
    void approve_shouldReturnApprovedBooking() throws Exception {
        Booking booking = createBooking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED
        );

        when(bookingService.approve(1L, 1L, true)).thenReturn(booking);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).approve(1L, 1L, true);
    }

    @Test
    void approve_shouldReturnRejectedBooking() throws Exception {
        Booking booking = createBooking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.REJECTED
        );

        when(bookingService.approve(1L, 1L, false)).thenReturn(booking);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(bookingService).approve(1L, 1L, false);
    }

    @Test
    void getById_shouldReturnBooking() throws Exception {
        Booking booking = createBooking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        when(bookingService.getById(2L, 1L)).thenReturn(booking);

        mockMvc.perform(get("/bookings/1")
                        .header(USER_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.item.id").value(10))
                .andExpect(jsonPath("$.booker.id").value(2));

        verify(bookingService).getById(2L, 1L);
    }

    @Test
    void getByBooker_shouldReturnAllBookings() throws Exception {
        Booking booking = createBooking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        when(bookingService.getByBooker(2L)).thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("WAITING"));

        verify(bookingService).getByBooker(2L);
    }

    @Test
    void getByOwner_shouldReturnAllBookings() throws Exception {
        Booking booking = createBooking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        when(bookingService.getByOwner(1L)).thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].item.name").value("Дрель"));

        verify(bookingService).getByOwner(1L);
    }

    @Test
    void getByBooker_shouldFilterWaitingBookings() throws Exception {
        Booking waiting = createBooking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        Booking approved = createBooking(
                2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED
        );

        when(bookingService.getByBooker(2L))
                .thenReturn(List.of(waiting, approved));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 2L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void getByBooker_shouldFilterRejectedBookings() throws Exception {
        Booking rejected = createBooking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.REJECTED
        );

        Booking approved = createBooking(
                2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED
        );

        when(bookingService.getByBooker(2L))
                .thenReturn(List.of(rejected, approved));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 2L)
                        .param("state", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("REJECTED"));
    }

    @Test
    void getByBooker_shouldFilterPastBookings() throws Exception {
        Booking past = createBooking(
                1L,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2),
                BookingStatus.APPROVED
        );

        Booking future = createBooking(
                2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED
        );

        when(bookingService.getByBooker(2L))
                .thenReturn(List.of(past, future));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 2L)
                        .param("state", "PAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getByBooker_shouldFilterFutureBookings() throws Exception {
        Booking future = createBooking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED
        );

        Booking past = createBooking(
                2L,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2),
                BookingStatus.APPROVED
        );

        when(bookingService.getByBooker(2L))
                .thenReturn(List.of(future, past));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 2L)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getByBooker_shouldFilterCurrentBookings() throws Exception {
        Booking current = createBooking(
                1L,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
                BookingStatus.APPROVED
        );

        Booking future = createBooking(
                2L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(3),
                BookingStatus.APPROVED
        );

        when(bookingService.getByBooker(2L))
                .thenReturn(List.of(current, future));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 2L)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }


    @Test
    void getByBooker_shouldReturnBadRequestForUnknownState() throws Exception {
        Booking booking = createBooking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        when(bookingService.getByBooker(2L))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 2L)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }


    private Booking createBooking(
            Long id,
            LocalDateTime start,
            LocalDateTime end,
            BookingStatus status
    ) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);

        ru.practicum.shareit.item.model.Item item =
                new ru.practicum.shareit.item.model.Item();
        item.setId(10L);
        item.setName("Дрель");
        booking.setItem(item);

        ru.practicum.shareit.user.User booker =
                new ru.practicum.shareit.user.User();
        booker.setId(2L);
        booking.setBooker(booker);

        return booking;
    }
}
