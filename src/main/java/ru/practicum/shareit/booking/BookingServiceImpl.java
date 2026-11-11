package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ItemRepository itemRepository,
                              UserService userService) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Booking create(Long bookerId, Booking booking) {
        User booker = userService.getById(bookerId);

        if (booking.getStart() == null || booking.getEnd() == null) {
            throw new ValidationException("Дата начала и окончания бронирования обязательны");
        }

        LocalDateTime now = LocalDateTime.now();

        if (!booking.getStart().isAfter(now)) {
            throw new ValidationException("Дата начала бронирования должна быть в будущем");
        }

        if (!booking.getEnd().isAfter(booking.getStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }

        if (booking.getItem() == null || booking.getItem().getId() == null) {
            throw new ValidationException("Не указана вещь для бронирования");
        }

        Item item = itemRepository.findById(booking.getItem().getId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new ValidationException("Нельзя бронировать собственную вещь");
        }

        booking.setId(null);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking approve(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = findBooking(bookingId);

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Бронирование с id=" + bookingId + " не найдено");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Можно изменить только ожидающее бронирование");
        }

        if (approved == null) {
            throw new ValidationException("Не указан результат подтверждения");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getById(Long userId, Long bookingId) {
        Booking booking = findBooking(bookingId);

        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();

        if (!ownerId.equals(userId) && !bookerId.equals(userId)) {
            throw new NotFoundException("Бронирование с id=" + bookingId + " не найдено");
        }

        return booking;
    }

    @Override
    public List<Booking> getByBooker(Long bookerId) {
        userService.getById(bookerId);
        return bookingRepository.findByBookerIdOrderByStartDesc(bookerId);
    }

    @Override
    public List<Booking> getByOwner(Long ownerId) {
        userService.getById(ownerId);
        return bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId);
    }

    private Booking findBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
    }
}