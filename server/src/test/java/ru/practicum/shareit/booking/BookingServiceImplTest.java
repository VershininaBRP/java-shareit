package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private User createUser(String name, String email) {
        return userService.create(new User(null, name, email));
    }

    private Item createItem(User owner, boolean available) {
        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Электрическая дрель");
        item.setAvailable(available);

        return itemService.create(owner.getId(), item);
    }

    private Booking createBooking(User booker, Item item) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        return bookingService.create(booker.getId(), booking);
    }

    @Test
    void create_shouldSaveBookingWithWaitingStatus() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, true);

        Booking booking = createBooking(booker, item);

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(booking.getItem().getId()).isEqualTo(item.getId());
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void create_shouldThrowExceptionWhenStartIsMissing() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, true);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setEnd(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_shouldThrowExceptionWhenStartIsInPast() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, true);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_shouldThrowExceptionWhenEndIsBeforeStart() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, true);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusDays(2));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_shouldThrowExceptionWhenItemIsUnavailable() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, false);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_shouldThrowExceptionWhenOwnerBooksOwnItem() {
        User owner = createUser("Анна", "anna@example.com");
        Item item = createItem(owner, true);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(owner.getId(), booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_shouldThrowExceptionWhenItemNotFound() {
        User booker = createUser("Иван", "ivan@example.com");

        Item item = new Item();
        item.setId(999L);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), booking))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void approve_shouldSetApprovedStatus() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, true);
        Booking booking = createBooking(booker, item);

        Booking approved = bookingService.approve(
                owner.getId(), booking.getId(), true
        );

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approve_shouldSetRejectedStatus() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, true);
        Booking booking = createBooking(booker, item);

        Booking rejected = bookingService.approve(
                owner.getId(), booking.getId(), false
        );

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approve_shouldThrowExceptionWhenUserIsNotOwner() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        User anotherUser = createUser("Олег", "oleg@example.com");
        Item item = createItem(owner, true);
        Booking booking = createBooking(booker, item);

        assertThatThrownBy(() -> bookingService.approve(
                anotherUser.getId(), booking.getId(), true
        )).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void approve_shouldThrowExceptionWhenBookingNotFound() {
        User owner = createUser("Анна", "anna@example.com");

        assertThatThrownBy(() -> bookingService.approve(
                owner.getId(), 999L, true
        )).isInstanceOf(NotFoundException.class);
    }

    @Test
    void approve_shouldThrowExceptionWhenBookingAlreadyApproved() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, true);
        Booking booking = createBooking(booker, item);

        bookingService.approve(owner.getId(), booking.getId(), true);

        assertThatThrownBy(() -> bookingService.approve(
                owner.getId(), booking.getId(), false
        )).isInstanceOf(ValidationException.class);
    }

    @Test
    void getById_shouldReturnBookingForBooker() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, true);
        Booking booking = createBooking(booker, item);

        Booking found = bookingService.getById(booker.getId(), booking.getId());

        assertThat(found.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getById_shouldReturnBookingForOwner() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        Item item = createItem(owner, true);
        Booking booking = createBooking(booker, item);

        Booking found = bookingService.getById(owner.getId(), booking.getId());

        assertThat(found.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getById_shouldThrowExceptionForAnotherUser() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        User anotherUser = createUser("Олег", "oleg@example.com");
        Item item = createItem(owner, true);
        Booking booking = createBooking(booker, item);

        assertThatThrownBy(() -> bookingService.getById(
                anotherUser.getId(), booking.getId()
        )).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_shouldThrowExceptionWhenBookingNotFound() {
        User user = createUser("Анна", "anna@example.com");

        assertThatThrownBy(() -> bookingService.getById(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getByBooker_shouldReturnOnlyBookerBookings() {
        User owner = createUser("Анна", "anna@example.com");
        User booker = createUser("Иван", "ivan@example.com");
        User anotherBooker = createUser("Олег", "oleg@example.com");

        Item firstItem = createItem(owner, true);
        Item secondItem = createItem(owner, true);

        createBooking(booker, firstItem);
        createBooking(anotherBooker, secondItem);

        List<Booking> bookings = bookingService.getByBooker(booker.getId());

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getBooker().getId())
                .isEqualTo(booker.getId());
    }

    @Test
    void getByOwner_shouldReturnBookingsForOwnerItems() {
        User owner = createUser("Анна", "anna@example.com");
        User anotherOwner = createUser("Олег", "oleg@example.com");
        User booker = createUser("Иван", "ivan@example.com");

        Item firstItem = createItem(owner, true);
        Item secondItem = createItem(anotherOwner, true);

        createBooking(booker, firstItem);
        createBooking(booker, secondItem);

        List<Booking> bookings = bookingService.getByOwner(owner.getId());

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getOwner().getId())
                .isEqualTo(owner.getId());
    }
}
