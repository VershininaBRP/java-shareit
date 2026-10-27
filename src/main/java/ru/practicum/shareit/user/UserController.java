package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserDto dto) {
        return UserMapper.toUserDto(userService.create(UserMapper.toUser(dto)));
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @RequestBody UserDto dto) {
        return UserMapper.toUserDto(userService.update(userId, UserMapper.toUser(dto)));
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable Long userId) {
        return UserMapper.toUserDto(userService.getById(userId));
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll().stream().map(UserMapper::toUserDto).toList();
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}
