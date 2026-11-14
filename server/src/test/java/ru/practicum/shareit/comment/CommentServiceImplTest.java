package ru.practicum.shareit.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceImplTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userService.create(user);
    }

    private Item createItem(User owner) {
        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Электрическая дрель");
        item.setAvailable(true);
        return itemService.create(owner.getId(), item);
    }

    private void createFinishedBooking(User booker, Item item) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);

        bookingRepository.save(booking);
    }

    @Test
    void create_shouldSaveCommentAfterFinishedBooking() {
        User owner = createUser("Владелец", "owner@test.ru");
        User author = createUser("Автор", "author@test.ru");
        Item item = createItem(owner);

        createFinishedBooking(author, item);

        Comment comment = new Comment();
        comment.setText("Отличная вещь!");

        Comment saved = commentService.create(author.getId(), item.getId(), comment);

        assertNotNull(saved.getId());
        assertEquals("Отличная вещь!", saved.getText());
        assertEquals(author.getId(), saved.getAuthor().getId());
        assertEquals(item.getId(), saved.getItem().getId());
        assertNotNull(saved.getCreated());
    }

    @Test
    void create_shouldThrowExceptionWhenTextIsBlank() {
        User author = createUser("Автор", "author@test.ru");
        Item item = createItem(createUser("Владелец", "owner@test.ru"));

        Comment comment = new Comment();
        comment.setText("   ");

        assertThrows(ValidationException.class,
                () -> commentService.create(author.getId(), item.getId(), comment));
    }

    @Test
    void create_shouldThrowExceptionWhenItemDoesNotExist() {
        User author = createUser("Автор", "author@test.ru");

        Comment comment = new Comment();
        comment.setText("Хорошая вещь");

        assertThrows(NotFoundException.class,
                () -> commentService.create(author.getId(), 999L, comment));
    }

    @Test
    void create_shouldThrowExceptionWhenThereIsNoFinishedBooking() {
        User author = createUser("Автор", "author@test.ru");
        Item item = createItem(createUser("Владелец", "owner@test.ru"));

        Comment comment = new Comment();
        comment.setText("Хорошая вещь");

        assertThrows(ValidationException.class,
                () -> commentService.create(author.getId(), item.getId(), comment));
    }

    @Test
    void getByItem_shouldReturnComments() {
        User owner = createUser("Владелец", "owner@test.ru");
        User author = createUser("Автор", "author@test.ru");
        Item item = createItem(owner);

        createFinishedBooking(author, item);

        Comment comment = new Comment();
        comment.setText("Всё понравилось!");
        commentService.create(author.getId(), item.getId(), comment);

        List<Comment> comments = commentService.getByItem(item.getId());

        assertEquals(1, comments.size());
        assertEquals("Всё понравилось!", comments.get(0).getText());
    }

    @Test
    void getByItem_shouldReturnEmptyListWhenThereAreNoComments() {
        Item item = createItem(createUser("Владелец", "owner@test.ru"));

        List<Comment> comments = commentService.getByItem(item.getId());

        assertTrue(comments.isEmpty());
    }

    @Test
    void getByItem_shouldThrowExceptionWhenItemDoesNotExist() {
        assertThrows(NotFoundException.class,
                () -> commentService.getByItem(999L));
    }
}
