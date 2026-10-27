package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item create(Long ownerId, Item item);

    Item update(Long ownerId, Long itemId, Item patch);

    Item getById(Long itemId);

    List<Item> getByOwner(Long ownerId);

    List<Item> search(String text);
}
