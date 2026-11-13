package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.ItemRequestClient;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void create_shouldReturnCreated() throws Exception {
        when(itemRequestClient.createRequest(anyLong(), any()))
                .thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "description": "Нужна дрель"
                            }
                            """))
                .andExpect(status().isCreated());

        verify(itemRequestClient).createRequest(eq(1L), any());
    }

    @Test
    void getOwnRequests_shouldReturnOk() throws Exception {
        when(itemRequestClient.getOwnRequests(1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(itemRequestClient).getOwnRequests(1L);
    }

    @Test
    void getAllRequests_shouldReturnOk() throws Exception {
        when(itemRequestClient.getAllRequests(1L, 0, 10))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(itemRequestClient).getAllRequests(1L, 0, 10);
    }

    @Test
    void getAllRequests_shouldPassPaginationParameters() throws Exception {
        when(itemRequestClient.getAllRequests(1L, 5, 3))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "5")
                        .param("size", "3"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getAllRequests(1L, 5, 3);
    }

    @Test
    void getRequestById_shouldReturnOk() throws Exception {
        when(itemRequestClient.getRequestById(10L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/10"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getRequestById(10L);
    }

    @Test
    void create_shouldReturnBadRequestWhenUserHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "description": "Нужна дрель"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}