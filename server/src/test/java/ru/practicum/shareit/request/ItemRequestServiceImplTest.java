package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createRequest_whenDescriptionIsValid_shouldSaveRequest() {
        User user = createUser("requester@example.com");

        ItemRequestDto result = itemRequestService.create(
                user.getId(),
                createRequestDto("Нужен шуруповёрт")
        );

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Нужен шуруповёрт");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void createRequest_whenDescriptionIsBlank_shouldThrowException() {
        User user = createUser("requester@example.com");

        assertThatThrownBy(() -> itemRequestService.create(
                user.getId(),
                createRequestDto("   ")
        )).isInstanceOf(ValidationException.class);
    }

    @Test
    void getOwnRequests_shouldReturnOnlyOwnRequestsInNewestFirstOrder() {
        User user = createUser("requester@example.com");
        User anotherUser = createUser("another@example.com");

        createStoredRequest(user, "Старый запрос", LocalDateTime.now().minusDays(2));
        createStoredRequest(user, "Новый запрос", LocalDateTime.now());
        createStoredRequest(anotherUser, "Чужой запрос", LocalDateTime.now());

        List<ItemRequestDto> result =
                itemRequestService.getOwnRequests(user.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ItemRequestDto::getDescription)
                .containsExactly("Новый запрос", "Старый запрос");
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequestsInNewestFirstOrder() {
        User user = createUser("requester@example.com");
        User anotherUser = createUser("another@example.com");

        createStoredRequest(user, "Мой запрос", LocalDateTime.now());
        createStoredRequest(anotherUser, "Старый чужой запрос",
                LocalDateTime.now().minusDays(2));
        createStoredRequest(anotherUser, "Новый чужой запрос",
                LocalDateTime.now());

        List<ItemRequestDto> result =
                itemRequestService.getAllRequests(user.getId(), 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ItemRequestDto::getDescription)
                .containsExactly("Новый чужой запрос", "Старый чужой запрос");
    }

    @Test
    void getAllRequests_shouldApplyPagination() {
        User user = createUser("requester@example.com");
        User anotherUser = createUser("another@example.com");

        createStoredRequest(anotherUser, "Первый", LocalDateTime.now().minusDays(3));
        createStoredRequest(anotherUser, "Второй", LocalDateTime.now().minusDays(2));
        createStoredRequest(anotherUser, "Третий", LocalDateTime.now().minusDays(1));

        List<ItemRequestDto> result =
                itemRequestService.getAllRequests(user.getId(), 1, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Второй");
    }

    @Test
    void getRequestById_whenRequestExists_shouldReturnRequest() {
        User user = createUser("requester@example.com");
        ItemRequest savedRequest = createStoredRequest(
                user,
                "Нужен велосипед",
                LocalDateTime.now()
        );

        ItemRequestDto result =
                itemRequestService.getRequestById(savedRequest.getId());

        assertThat(result.getId()).isEqualTo(savedRequest.getId());
        assertThat(result.getDescription()).isEqualTo("Нужен велосипед");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void getRequestById_whenRequestDoesNotExist_shouldThrowException() {
        assertThatThrownBy(() -> itemRequestService.getRequestById(-1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getRequestById_shouldReturnItemsCreatedForRequest() {
        User requestor = createUser("requestor@example.com");
        User owner = createUser("owner@example.com");

        ItemRequest request = createStoredRequest(
                requestor,
                "Нужен велосипед",
                LocalDateTime.now()
        );

        Item item = new Item();
        item.setName("Горный велосипед");
        item.setDescription("Велосипед для поездок");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);

        Item savedItem = itemRepository.save(item);

        ItemRequestDto result =
                itemRequestService.getRequestById(request.getId());

        assertThat(result.getItems()).hasSize(1);

        assertThat(result.getItems().get(0).getId())
                .isEqualTo(savedItem.getId());

        assertThat(result.getItems().get(0).getName())
                .isEqualTo("Горный велосипед");

        assertThat(result.getItems().get(0).getOwnerId())
                .isEqualTo(owner.getId());
    }

    private User createUser(String email) {
        User user = new User();
        user.setName("Тестовый пользователь");
        user.setEmail(email);
        return userRepository.save(user);
    }

    private ItemRequestDto createRequestDto(String description) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription(description);
        return dto;
    }

    private ItemRequest createStoredRequest(
            User user,
            String description,
            LocalDateTime created
    ) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(user);
        request.setCreated(created);
        return itemRequestRepository.save(request);
    }
}
