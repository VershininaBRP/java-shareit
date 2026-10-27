
package ru.practicum.shareit.item;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ItemServiceImpl implements ItemService {

    private final Map<Long, Item> items = new LinkedHashMap<>();
    private final AtomicLong ids = new AtomicLong(1);
    private final UserService userService;

    public ItemServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Item create(Long ownerId, Item item) {
        User owner = userService.getById(ownerId);
        validateItem(item.getName(), item.getDescription());

        if (item.getAvailable() == null) {
            throw new ValidationException(
                    "Необходимо указать доступность вещи"
            );
        }

        Item saved = new Item(
                ids.getAndIncrement(),
                item.getName(),
                item.getDescription(),
                Boolean.TRUE.equals(item.getAvailable()),
                owner,
                null
        );

        items.put(saved.getId(), saved);
        return copy(saved);
    }

    @Override
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

        boolean available = patch.getAvailable() == null
                ? Boolean.TRUE.equals(current.getAvailable())
                : patch.getAvailable();

        Item updated = new Item(
                itemId,
                name,
                description,
                available,
                current.getOwner(),
                current.getRequest()
        );

        items.put(itemId, updated);
        return copy(updated);
    }

    @Override
    public Item getById(Long itemId) {
        return copy(getStored(itemId));
    }

    @Override
    public List<Item> getByOwner(Long ownerId) {
        userService.getById(ownerId);

        List<Item> result = new ArrayList<>();

        items.values().stream()
                .filter(i -> i.getOwner().getId().equals(ownerId))
                .forEach(i -> result.add(copy(i)));

        return result;
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String query = text.toLowerCase(Locale.ROOT);
        List<Item> result = new ArrayList<>();

        items.values().stream()
                .filter(i -> Boolean.TRUE.equals(i.getAvailable()))
                .filter(i ->
                        i.getName().toLowerCase(Locale.ROOT).contains(query)
                                || i.getDescription()
                                .toLowerCase(Locale.ROOT).contains(query))
                .forEach(i -> result.add(copy(i)));

        return result;
    }

    private Item getStored(Long id) {
        Item item = items.get(id);

        if (item == null) {
            throw new NotFoundException(
                    "Вещь с идентификатором " + id + " не найдена"
            );
        }

        return item;
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

    private Item copy(Item i) {
        return new Item(
                i.getId(),
                i.getName(),
                i.getDescription(),
                i.getAvailable(),
                i.getOwner(),
                i.getRequest()
        );
    }
}
