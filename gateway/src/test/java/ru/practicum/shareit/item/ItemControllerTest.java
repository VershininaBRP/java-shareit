
package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.ItemClient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Test
    void create_shouldReturnCreated() throws Exception {
        when(itemClient.createItem(anyLong(), any()))
                .thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + """
                                "name": "Дрель",
                                "description": "Электрическая дрель",
                                "available": true
                                }
                                """))
                .andExpect(status().isCreated());

        verify(itemClient).createItem(eq(1L), any());
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        when(itemClient.updateItem(anyLong(), anyLong(), any()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/10")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + """
                                "name": "Новая дрель",
                                "description": "Обновлённое описание"
                                }
                                """))
                .andExpect(status().isOk());

        verify(itemClient).updateItem(eq(1L), eq(10L), any());
    }

    @Test
    void getItem_shouldReturnOk() throws Exception {
        when(itemClient.getItem(1L, 10L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/10")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(itemClient).getItem(1L, 10L);
    }

    @Test
    void getItemsByOwner_shouldReturnOk() throws Exception {
        when(itemClient.getItemsByOwner(1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(itemClient).getItemsByOwner(1L);
    }

    @Test
    void search_shouldReturnOk() throws Exception {
        when(itemClient.searchItems("дрель"))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());

        verify(itemClient).searchItems("дрель");
    }
}
