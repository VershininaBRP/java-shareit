package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void create_shouldReturnCreatedUser() throws Exception {
        UserDto input = createUserDto(null, "Анна", "anna@example.com");
        User savedUser = createUser(1L, "Анна", "anna@example.com");

        when(userService.create(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Анна"))
                .andExpect(jsonPath("$.email").value("anna@example.com"));

        verify(userService).create(any(User.class));
    }

    @Test
    void update_shouldReturnUpdatedUser() throws Exception {
        UserDto input = createUserDto(1L, "Анна Петрова", "anna@example.com");
        User updatedUser = createUser(1L, "Анна Петрова", "anna@example.com");

        when(userService.update(eq(1L), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Анна Петрова"))
                .andExpect(jsonPath("$.email").value("anna@example.com"));

        verify(userService).update(eq(1L), any(User.class));
    }

    @Test
    void getById_shouldReturnUser() throws Exception {
        User user = createUser(1L, "Анна", "anna@example.com");

        when(userService.getById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Анна"))
                .andExpect(jsonPath("$.email").value("anna@example.com"));

        verify(userService).getById(1L);
    }

    @Test
    void getAll_shouldReturnUsers() throws Exception {
        User first = createUser(1L, "Анна", "anna@example.com");
        User second = createUser(2L, "Иван", "ivan@example.com");

        when(userService.getAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Анна"))
                .andExpect(jsonPath("$[1].name").value("Иван"));

        verify(userService).getAll();
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    private UserDto createUserDto(Long id, String name, String email) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    private User createUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
