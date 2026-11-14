package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void create_shouldReturnCreatedRequest() throws Exception {
        ItemRequestDto input = new ItemRequestDto();
        input.setDescription("Нужен велосипед");

        ItemRequestDto output = new ItemRequestDto(
                1L,
                "Нужен велосипед",
                LocalDateTime.of(2026, 11, 13, 10, 0),
                List.of()
        );

        when(itemRequestService.create(eq(1L), any(ItemRequestDto.class)))
                .thenReturn(output);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужен велосипед"))
                .andExpect(jsonPath("$.items").isArray());

        verify(itemRequestService).create(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    void getOwnRequests_shouldReturnRequests() throws Exception {
        ItemRequestDto request = new ItemRequestDto(
                1L,
                "Нужен велосипед",
                LocalDateTime.of(2026, 11, 13, 10, 0),
                List.of()
        );

        when(itemRequestService.getOwnRequests(1L))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Нужен велосипед"));

        verify(itemRequestService).getOwnRequests(1L);
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() throws Exception {
        ItemRequestDto request = new ItemRequestDto(
                2L,
                "Нужна дрель",
                LocalDateTime.of(2026, 11, 12, 10, 0),
                List.of()
        );

        when(itemRequestService.getAllRequests(1L, 5, 10))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1L)
                        .param("from", "5")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"));

        verify(itemRequestService).getAllRequests(1L, 5, 10);
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        ItemRequestDto request = new ItemRequestDto(
                3L,
                "Нужен шуруповёрт",
                LocalDateTime.of(2026, 11, 11, 10, 0),
                List.of()
        );

        when(itemRequestService.getRequestById(3L))
                .thenReturn(request);

        mockMvc.perform(get("/requests/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.description").value("Нужен шуруповёрт"));

        verify(itemRequestService).getRequestById(3L);
    }
}
