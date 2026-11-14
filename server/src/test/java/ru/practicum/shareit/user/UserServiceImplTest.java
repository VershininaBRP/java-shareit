package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_shouldSaveUser() {
        User user = new User(null, "Анна", "anna@example.com");

        User savedUser = userService.create(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("Анна");
        assertThat(savedUser.getEmail()).isEqualTo("anna@example.com");
        assertThat(userRepository.findById(savedUser.getId())).isPresent();
    }

    @Test
    void create_shouldThrowExceptionWhenEmailIsInvalid() {
        User user = new User(null, "Анна", "invalid-email");

        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_shouldThrowExceptionWhenEmailAlreadyExists() {
        userService.create(new User(null, "Анна", "anna@example.com"));

        User anotherUser = new User(null, "Иван", "anna@example.com");

        assertThatThrownBy(() -> userService.create(anotherUser))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void update_shouldChangeOnlyProvidedFields() {
        User savedUser = userService.create(
                new User(null, "Анна", "anna@example.com")
        );

        User patch = new User();
        patch.setName("Анна Иванова");

        User updatedUser = userService.update(savedUser.getId(), patch);

        assertThat(updatedUser.getName()).isEqualTo("Анна Иванова");
        assertThat(updatedUser.getEmail()).isEqualTo("anna@example.com");
    }

    @Test
    void update_shouldChangeEmail() {
        User savedUser = userService.create(
                new User(null, "Анна", "anna@example.com")
        );

        User patch = new User();
        patch.setEmail("new@example.com");

        User updatedUser = userService.update(savedUser.getId(), patch);

        assertThat(updatedUser.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void update_shouldThrowExceptionWhenUserNotFound() {
        User patch = new User();
        patch.setName("Новое имя");

        assertThatThrownBy(() -> userService.update(999L, patch))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_shouldReturnUser() {
        User savedUser = userService.create(
                new User(null, "Анна", "anna@example.com")
        );

        User foundUser = userService.getById(savedUser.getId());

        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getName()).isEqualTo("Анна");
    }

    @Test
    void getById_shouldThrowExceptionWhenUserNotFound() {
        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        userService.create(new User(null, "Анна", "anna@example.com"));
        userService.create(new User(null, "Иван", "ivan@example.com"));

        List<User> users = userService.getAll();

        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder(
                        "anna@example.com",
                        "ivan@example.com"
                );
    }

    @Test
    void delete_shouldRemoveUser() {
        User savedUser = userService.create(
                new User(null, "Анна", "anna@example.com")
        );

        userService.delete(savedUser.getId());

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    void delete_shouldThrowExceptionWhenUserNotFound() {
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
