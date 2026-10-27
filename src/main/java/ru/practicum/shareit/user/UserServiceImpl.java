
package ru.practicum.shareit.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new LinkedHashMap<>();
    private final AtomicLong ids = new AtomicLong(1);

    @Override
    public User create(User user) {
        validateEmail(user.getEmail(), null);

        user.setId(ids.getAndIncrement());

        User saved = new User(
                user.getId(),
                user.getName(),
                user.getEmail()
        );

        users.put(saved.getId(), saved);
        return copy(saved);
    }

    @Override
    public User update(Long userId, User patch) {
        User current = getById(userId);

        String email = patch.getEmail() == null
                ? current.getEmail() : patch.getEmail();

        validateEmail(email, userId);

        String name = patch.getName() == null
                ? current.getName() : patch.getName();

        User updated = new User(userId, name, email);
        users.put(userId, updated);

        return copy(updated);
    }

    @Override
    public User getById(Long userId) {
        User user = users.get(userId);

        if (user == null) {
            throw new NotFoundException(
                    "Пользователь с идентификатором " + userId + " не найден"
            );
        }

        return copy(user);
    }

    @Override
    public List<User> getAll() {
        List<User> result = new ArrayList<>();
        users.values().forEach(user -> result.add(copy(user)));
        return result;
    }

    @Override
    public void delete(Long userId) {
        if (users.remove(userId) == null) {
            throw new NotFoundException(
                    "Пользователь с идентификатором " + userId + " не найден"
            );
        }
    }

    private void validateEmail(String email, Long excludedId) {
        if (email == null || email.isBlank()
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ValidationException("Указан некорректный адрес электронной почты");
        }

        boolean exists = users.values().stream()
                .anyMatch(u ->
                        u.getEmail().equalsIgnoreCase(email)
                                && !u.getId().equals(excludedId));

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Пользователь с таким адресом электронной почты уже существует: " + email
            );
        }
    }

    private User copy(User u) {
        return new User(u.getId(), u.getName(), u.getEmail());
    }
}
