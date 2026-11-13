package ru.practicum.shareit.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.CommentClient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentClient commentClient;

    @Test
    void createComment_shouldReturnCreated() throws Exception {
        when(commentClient.createComment(anyLong(), anyLong(), any()))
                .thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post("/items/10/comment")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "text": "Отличная вещь!"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(commentClient).createComment(eq(1L), eq(10L), any());
    }

    @Test
    void getComments_shouldReturnOk() throws Exception {
        when(commentClient.getComments(10L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/10/comment"))
                .andExpect(status().isOk());

        verify(commentClient).getComments(10L);
    }
}
