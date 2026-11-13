package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void create_shouldReturnCreatedItem() throws Exception {
        ItemDto input = createItemDto(
                null, "Дрель", "Для ремонта", true, 15L
        );

        Item savedItem = createItem(
                1L, "Дрель", "Для ремонта", true, 15L
        );

        when(itemService.create(eq(1L), any(Item.class)))
                .thenReturn(savedItem);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Для ремонта"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.requestId").value(15));

        verify(itemService).create(
                eq(1L),
                argThat(item -> item.getRequest() != null
                        && item.getRequest().getId().equals(15L))
        );
    }

    @Test
    void update_shouldReturnUpdatedItem() throws Exception {
        ItemDto input = createItemDto(
                1L, "Обновлённая дрель", "Для ремонта", false, null
        );

        Item updatedItem = createItem(
                1L, "Обновлённая дрель", "Для ремонта", false, null
        );

        when(itemService.update(eq(1L), eq(1L), any(Item.class)))
                .thenReturn(updatedItem);

        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Обновлённая дрель"))
                .andExpect(jsonPath("$.available").value(false));

        verify(itemService).update(
                eq(1L), eq(1L), any(Item.class)
        );
    }

    @Test
    void getById_shouldReturnItem() throws Exception {
        ItemDto item = createItemDto(
                1L, "Дрель", "Для ремонта", true, null
        );

        when(itemService.getByIdDto(1L, 1L))
                .thenReturn(item);

        mockMvc.perform(get("/items/1")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Для ремонта"));

        verify(itemService).getByIdDto(1L, 1L);
    }

    @Test
    void getByOwner_shouldReturnOwnerItems() throws Exception {
        ItemDto first = createItemDto(
                1L, "Дрель", "Для ремонта", true, null
        );

        ItemDto second = createItemDto(
                2L, "Молоток", "Для строительства", true, null
        );

        when(itemService.getByOwnerDto(1L))
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[1].name").value("Молоток"));

        verify(itemService).getByOwnerDto(1L);
    }

    @Test
    void search_shouldReturnFoundItems() throws Exception {
        ItemDto item = createItemDto(
                1L, "Дрель", "Для ремонта", true, null
        );

        when(itemService.searchDto("дрель"))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));

        verify(itemService).searchDto("дрель");
    }

    private ItemDto createItemDto(
            Long id,
            String name,
            String description,
            Boolean available,
            Long requestId
    ) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        dto.setRequestId(requestId);
        return dto;
    }

    private Item createItem(
            Long id,
            String name,
            String description,
            Boolean available,
            Long requestId
    ) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);

        if (requestId != null) {
            ItemRequest request = new ItemRequest();
            request.setId(requestId);
            item.setRequest(request);
        }

        return item;
    }
}
