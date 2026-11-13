package ru.practicum.shareit.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDto;

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

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void create_shouldReturnCreatedComment() throws Exception {
        CommentDto input = new CommentDto();
        input.setText("Отличная дрель!");

        when(commentService.create(eq(1L), eq(10L), any(Comment.class)))
                .thenReturn(createComment(
                        1L, "Отличная дрель!", "Анна"
                ));

        mockMvc.perform(post("/items/10/comment")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Отличная дрель!"))
                .andExpect(jsonPath("$.authorName").value("Анна"))
                .andExpect(jsonPath("$.created").exists());

        verify(commentService).create(eq(1L), eq(10L), any(Comment.class));
    }

    @Test
    void getByItem_shouldReturnComments() throws Exception {
        Comment first = createComment(
                1L, "Отличная дрель!", "Анна"
        );

        Comment second = createComment(
                2L, "Всё работает!", "Иван"
        );

        when(commentService.getByItem(10L))
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/items/10/comment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("Отличная дрель!"))
                .andExpect(jsonPath("$[0].authorName").value("Анна"))
                .andExpect(jsonPath("$[1].text").value("Всё работает!"))
                .andExpect(jsonPath("$[1].authorName").value("Иван"));

        verify(commentService).getByItem(10L);
    }

    private Comment createComment(Long id, String text, String authorName) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setText(text);
        comment.setCreated(LocalDateTime.of(2026, 11, 13, 10, 0));

        ru.practicum.shareit.user.User author =
                new ru.practicum.shareit.user.User();
        author.setId(id);
        author.setName(authorName);
        comment.setAuthor(author);

        return comment;
    }
}
