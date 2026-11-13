
package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository,
                                  ItemRepository itemRepository,
                                  UserService userService) {
        this.itemRequestRepository = itemRequestRepository;
        this.itemRepository = itemRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public ItemRequestDto create(Long requestorId, ItemRequestDto dto) {
        if (dto == null || dto.getDescription() == null
                || dto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса не должно быть пустым");
        }

        User requestor = userService.getById(requestorId);

        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(request);

        return toDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(Long requestorId) {
        userService.getById(requestorId);

        return itemRequestRepository
                .findAllByRequestorIdOrderByCreatedDesc(requestorId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long requestorId, int from, int size) {
        userService.getById(requestorId);

        if (from < 0 || size <= 0) {
            throw new ValidationException(
                    "Параметры from и size должны быть корректными"
            );
        }

        int pageNumber = from / size;

        Page<ItemRequest> page = itemRequestRepository
                .findAllByRequestorIdNotOrderByCreatedDesc(
                        requestorId,
                        PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "created"))
                );

        return page.getContent().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId) {
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        "Запрос с id=" + requestId + " не найден"
                ));

        return toDto(request);
    }

    private ItemRequestDto toDto(ItemRequest request) {
        List<ItemRequestItemDto> items = itemRepository
                .findByRequestId(request.getId())
                .stream()
                .map(this::toItemDto)
                .toList();

        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                items
        );
    }

    private ItemRequestItemDto toItemDto(Item item) {
        return new ItemRequestItemDto(
                item.getId(),
                item.getName(),
                item.getOwner().getId()
        );
    }
}
