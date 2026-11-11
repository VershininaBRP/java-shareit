package ru.practicum.shareit.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User create(User user) {
        validateEmail(user.getEmail(), null);

        user.setId(null);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(Long userId, User patch) {
        User current = findUser(userId);

        String email = patch.getEmail() == null
                ? current.getEmail() : patch.getEmail();

        validateEmail(email, userId);

        String name = patch.getName() == null
                ? current.getName() : patch.getName();

        current.setName(name);
        current.setEmail(email);

        return userRepository.save(current);
    }

    @Override
    public User getById(Long userId) {
        return findUser(userId);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        User user = findUser(userId);
        userRepository.delete(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с идентификатором " + userId + " не найден"
                ));
    }

    private void validateEmail(String email, Long excludedId) {
        if (email == null || email.isBlank()
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ValidationException(
                    "Указан некорректный адрес электронной почты"
            );
        }

        boolean exists = excludedId == null
                ? userRepository.existsByEmailIgnoreCase(email)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(email, excludedId);

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Пользователь с таким адресом электронной почты уже существует: " + email
            );
        }
    }
}