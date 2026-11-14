package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRepository itemRepository;

    private User createUser() {
        return userService.create(
                new User(null, "Анна", "anna@example.com")
        );
    }

    private Item createItem(Long ownerId, String name, Boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription("Описание вещи");
        item.setAvailable(available);

        return itemService.create(ownerId, item);
    }

    @Test
    void create_shouldSaveItemWithoutRequest() {
        User owner = createUser();

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Электрическая дрель");
        item.setAvailable(true);

        Item savedItem = itemService.create(owner.getId(), item);

        assertThat(savedItem.getId()).isNotNull();
        assertThat(savedItem.getName()).isEqualTo("Дрель");
        assertThat(savedItem.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(savedItem.getRequest()).isNull();
        assertThat(itemRepository.findById(savedItem.getId())).isPresent();
    }

    @Test
    void create_shouldSaveItemWithRequest() {
        User owner = createUser();

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        ItemRequestDto savedRequest = itemRequestService.create(
                owner.getId(), requestDto
        );

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Электрическая дрель");
        item.setAvailable(true);

        ItemRequest request = new ItemRequest();
        request.setId(savedRequest.getId());
        item.setRequest(request);

        Item savedItem = itemService.create(owner.getId(), item);

        assertThat(savedItem.getRequest()).isNotNull();
        assertThat(savedItem.getRequest().getId())
                .isEqualTo(savedRequest.getId());
    }

    @Test
    void create_shouldThrowExceptionWhenRequestNotFound() {
        User owner = createUser();

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(true);

        ItemRequest request = new ItemRequest();
        request.setId(999L);
        item.setRequest(request);

        assertThatThrownBy(() -> itemService.create(owner.getId(), item))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_shouldThrowExceptionWhenNameIsBlank() {
        User owner = createUser();

        Item item = new Item();
        item.setName(" ");
        item.setDescription("Описание");
        item.setAvailable(true);

        assertThatThrownBy(() -> itemService.create(owner.getId(), item))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_shouldThrowExceptionWhenDescriptionIsBlank() {
        User owner = createUser();

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription(" ");
        item.setAvailable(true);

        assertThatThrownBy(() -> itemService.create(owner.getId(), item))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_shouldThrowExceptionWhenAvailabilityIsNull() {
        User owner = createUser();

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(null);

        assertThatThrownBy(() -> itemService.create(owner.getId(), item))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void update_shouldChangeItemFields() {
        User owner = createUser();
        Item savedItem = createItem(owner.getId(), "Дрель", true);

        Item patch = new Item();
        patch.setName("Новая дрель");
        patch.setAvailable(false);

        Item updatedItem = itemService.update(
                owner.getId(), savedItem.getId(), patch
        );

        assertThat(updatedItem.getName()).isEqualTo("Новая дрель");
        assertThat(updatedItem.getDescription()).isEqualTo("Описание вещи");
        assertThat(updatedItem.getAvailable()).isFalse();
    }

    @Test
    void update_shouldThrowExceptionWhenUserIsNotOwner() {
        User owner = createUser();
        User anotherUser = userService.create(
                new User(null, "Иван", "ivan@example.com")
        );

        Item savedItem = createItem(owner.getId(), "Дрель", true);

        Item patch = new Item();
        patch.setName("Новое название");

        assertThatThrownBy(() -> itemService.update(
                anotherUser.getId(), savedItem.getId(), patch
        )).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void update_shouldThrowExceptionWhenItemNotFound() {
        User owner = createUser();

        Item patch = new Item();
        patch.setName("Новое название");

        assertThatThrownBy(() -> itemService.update(
                owner.getId(), 999L, patch
        )).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_shouldReturnItem() {
        User owner = createUser();
        Item savedItem = createItem(owner.getId(), "Дрель", true);

        Item foundItem = itemService.getById(savedItem.getId());

        assertThat(foundItem.getId()).isEqualTo(savedItem.getId());
        assertThat(foundItem.getName()).isEqualTo("Дрель");
    }

    @Test
    void getByOwner_shouldReturnOnlyOwnerItems() {
        User owner = createUser();
        User anotherUser = userService.create(
                new User(null, "Иван", "ivan@example.com")
        );

        createItem(owner.getId(), "Дрель", true);
        createItem(owner.getId(), "Молоток", true);
        createItem(anotherUser.getId(), "Пила", true);

        List<Item> items = itemService.getByOwner(owner.getId());

        assertThat(items).hasSize(2);
        assertThat(items)
                .extracting(Item::getName)
                .containsExactlyInAnyOrder("Дрель", "Молоток");
    }

    @Test
    void search_shouldReturnOnlyAvailableMatchingItems() {
        User owner = createUser();

        createItem(owner.getId(), "Электрическая дрель", true);
        createItem(owner.getId(), "Дрель недоступна", false);
        createItem(owner.getId(), "Молоток", true);

        List<Item> items = itemService.search("дрель");

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName())
                .isEqualTo("Электрическая дрель");
    }

    @Test
    void search_shouldReturnEmptyListForBlankText() {
        assertThat(itemService.search(" ")).isEmpty();
    }

    @Test
    void getByIdDto_shouldReturnItemWithCommentsList() {
        User owner = createUser();
        Item savedItem = createItem(owner.getId(), "Дрель", true);

        ItemDto dto = itemService.getByIdDto(
                savedItem.getId(), owner.getId()
        );

        assertThat(dto.getId()).isEqualTo(savedItem.getId());
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getComments()).isEmpty();
        assertThat(dto.getRequestId()).isNull();
    }

    @Test
    void getByOwnerDto_shouldReturnOwnerItems() {
        User owner = createUser();
        createItem(owner.getId(), "Дрель", true);
        createItem(owner.getId(), "Молоток", true);

        List<ItemDto> items = itemService.getByOwnerDto(owner.getId());

        assertThat(items).hasSize(2);
        assertThat(items)
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Дрель", "Молоток");
    }

    @Test
    void searchDto_shouldReturnMatchingItems() {
        User owner = createUser();
        createItem(owner.getId(), "Электрическая дрель", true);
        createItem(owner.getId(), "Молоток", true);

        List<ItemDto> items = itemService.searchDto("дрель");

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName())
                .isEqualTo("Электрическая дрель");
    }
}
