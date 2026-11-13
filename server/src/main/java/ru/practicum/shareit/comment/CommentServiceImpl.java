package ru.practicum.shareit.comment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
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
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              BookingRepository bookingRepository,
                              ItemRepository itemRepository,
                              UserService userService) {
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Comment create(Long authorId, Long itemId, Comment comment) {
        User author = userService.getById(authorId);

        if (comment.getText() == null || comment.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        boolean hasFinishedBooking =
                bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                        itemId,
                        authorId,
                        BookingStatus.APPROVED,
                        LocalDateTime.now()
                );

        if (!hasFinishedBooking) {
            throw new ValidationException(
                    "Оставить комментарий можно только после завершения бронирования"
            );
        }

        comment.setId(null);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getByItem(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Вещь не найдена");
        }

        return commentRepository.findByItemIdOrderByCreatedDesc(itemId);
    }
}