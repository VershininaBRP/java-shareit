package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {

    Booking create(Long bookerId, Booking booking);

    Booking approve(Long ownerId, Long bookingId, Boolean approved);

    Booking getById(Long userId, Long bookingId);

    List<Booking> getByBooker(Long bookerId);

    List<Booking> getByOwner(Long ownerId);
}