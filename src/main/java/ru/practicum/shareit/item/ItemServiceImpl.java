package ru.practicum.shareit.item;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentService;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentService commentService;
    private final ItemRequestRepository itemRequestRepository;

    public ItemServiceImpl(ItemRepository itemRepository,
                           UserService userService,
                           BookingRepository bookingRepository,
                           CommentService commentService,
                           ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
        this.commentService = commentService;
        this.itemRequestRepository = itemRequestRepository;
    }


    @Override
    @Transactional
    public Item create(Long ownerId, Item item) {
        User owner = userService.getById(ownerId);

        validateItem(item.getName(), item.getDescription());

        if (item.getAvailable() == null) {
            throw new ValidationException(
                    "Необходимо указать доступность вещи"
            );
        }

        ItemRequest request = null;

        if (item.getRequest() != null) {
            Long requestId = item.getRequest().getId();

            request = itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException(
                            "Запрос с id=" + requestId + " не найден"
                    ));
        }

        Item saved = new Item(
                null,
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                owner,
                request
        );

        return itemRepository.save(saved);
    }


    @Override
    @Transactional
    public Item update(Long ownerId, Long itemId, Item patch) {
        Item current = getStored(itemId);

        if (!current.getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Только владелец вещи может её обновлять"
            );
        }

        String name = patch.getName() == null
                ? current.getName() : patch.getName();

        String description = patch.getDescription() == null
                ? current.getDescription() : patch.getDescription();

        validateItem(name, description);

        Boolean available = patch.getAvailable() == null
                ? current.getAvailable() : patch.getAvailable();

        current.setName(name);
        current.setDescription(description);
        current.setAvailable(available);

        return itemRepository.save(current);
    }

    @Override
    public Item getById(Long itemId) {
        return getStored(itemId);
    }

    @Override
    public List<Item> getByOwner(Long ownerId) {
        userService.getById(ownerId);
        return itemRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.searchAvailableItems(text.trim());
    }

    private Item getStored(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Вещь с идентификатором " + id + " не найдена"
                ));
    }

    private void validateItem(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new ValidationException(
                    "Название вещи не должно быть пустым"
            );
        }

        if (description == null || description.isBlank()) {
            throw new ValidationException(
                    "Описание вещи не должно быть пустым"
            );
        }
    }

    private ItemDto enrichItem(Item item, boolean isOwner) {
        ItemDto dto = ItemMapper.toItemDto(item);

        List<CommentDto> comments = commentService.getByItem(item.getId())
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        dto.setComments(comments);

        if (isOwner) {
            LocalDateTime now = LocalDateTime.now();

            Booking lastBooking = bookingRepository
                    .findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(
                            item.getId(), BookingStatus.APPROVED, now)
                    .orElse(null);

            Booking nextBooking = bookingRepository
                    .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                            item.getId(), BookingStatus.APPROVED, now)
                    .orElse(null);

            dto.setLastBooking(lastBooking == null
                    ? null
                    : new BookingShortDto(lastBooking.getId(), lastBooking.getBooker().getId()));

            dto.setNextBooking(nextBooking == null
                    ? null
                    : new BookingShortDto(nextBooking.getId(), nextBooking.getBooker().getId()));
        }

        return dto;
    }

    @Override
    public ItemDto getByIdDto(Long itemId, Long userId) {
        Item item = getStored(itemId);
        return enrichItem(item, item.getOwner().getId().equals(userId));
    }

    @Override
    public List<ItemDto> getByOwnerDto(Long ownerId) {
        userService.getById(ownerId);

        return itemRepository.findByOwnerId(ownerId).stream()
                .map(item -> enrichItem(item, true))
                .toList();
    }

    @Override
    public List<ItemDto> searchDto(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.searchAvailableItems(text.trim()).stream()
                .map(item -> enrichItem(item, false))
                .toList();
    }
}