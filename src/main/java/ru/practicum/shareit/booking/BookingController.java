package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto create(
            @RequestHeader("X-Sharer-User-Id") Long bookerId,
            @Valid @RequestBody BookingDto bookingDto) {

        Booking booking = BookingMapper.toBooking(bookingDto);
        return BookingMapper.toBookingDto(bookingService.create(bookerId, booking));
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {

        return BookingMapper.toBookingDto(
                bookingService.approve(ownerId, bookingId, approved));
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId) {

        return BookingMapper.toBookingDto(bookingService.getById(userId, bookingId));
    }

    @GetMapping
    public List<BookingDto> getByBooker(
            @RequestHeader("X-Sharer-User-Id") Long bookerId,
            @RequestParam(defaultValue = "ALL") String state) {

        return bookingService.getByBooker(bookerId).stream()
                .filter(booking -> matchesState(booking, state))
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(defaultValue = "ALL") String state) {

        return bookingService.getByOwner(ownerId).stream()
                .filter(booking -> matchesState(booking, state))
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private boolean matchesState(Booking booking, String state) {
        return switch (state.toUpperCase()) {
            case "ALL" -> true;
            case "CURRENT" -> !booking.getStart().isAfter(java.time.LocalDateTime.now())
                    && !booking.getEnd().isBefore(java.time.LocalDateTime.now());
            case "PAST" -> booking.getEnd().isBefore(java.time.LocalDateTime.now());
            case "FUTURE" -> booking.getStart().isAfter(java.time.LocalDateTime.now());
            case "WAITING" -> booking.getStatus() == BookingStatus.WAITING;
            case "REJECTED" -> booking.getStatus() == BookingStatus.REJECTED;
            default -> throw new ru.practicum.shareit.exception.ValidationException(
                    "Неизвестное состояние бронирования: " + state);
        };
    }
}