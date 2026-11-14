package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long requestorId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getOwnRequests(Long requestorId);

    List<ItemRequestDto> getAllRequests(Long requestorId, int from, int size);

    ItemRequestDto getRequestById(Long requestId);
}
