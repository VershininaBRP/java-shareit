package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Item;

public class BookingMapper {

    private BookingMapper() {
    }

    public static Booking toBooking(BookingDto dto) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());

        Item item = new Item();
        item.setId(dto.getItemId());
        booking.setItem(item);

        return booking;
    }

    public static BookingDto toBookingDto(Booking booking) {
        BookingItemDto itemDto = new BookingItemDto(
                booking.getItem().getId(),
                booking.getItem().getName()
        );

        BookingBookerDto bookerDto = new BookingBookerDto(
                booking.getBooker().getId()
        );

        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                null,
                itemDto,
                bookerDto,
                booking.getStatus()
        );
    }
}