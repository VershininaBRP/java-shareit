package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(
            @RequestHeader(USER_HEADER) Long userId,
            @Valid @RequestBody ItemDto dto
    ) {
        return ItemMapper.toItemDto(
                itemService.create(userId, ItemMapper.toItem(dto))
        );
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(
            @RequestHeader(USER_HEADER) Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto dto
    ) {
        return ItemMapper.toItemDto(
                itemService.update(userId, itemId, ItemMapper.toItem(dto))
        );
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Long itemId) {
        return ItemMapper.toItemDto(itemService.getById(itemId));
    }

    @GetMapping
    public List<ItemDto> getByOwner(@RequestHeader(USER_HEADER) Long userId) {
        return itemService.getByOwner(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestParam(defaultValue = "") String text
    ) {
        return itemService.search(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }
}
