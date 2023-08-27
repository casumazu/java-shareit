package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("Получен запрос GET на получение всех пользователей");
        return userClient.getUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> findById(@PathVariable Long userId) {
        log.info("Получен запрос GET на получение пользователя по id {}", userId);
        return userClient.getUserById(userId);
    }

    @ResponseBody
    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        log.info("Получен запрос POST на создание пользователя {}", userDto.toString());
        return userClient.create(userDto);
    }

    @ResponseBody
    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@RequestBody UserDto userDto, @PathVariable Long userId) {
        log.info("Получен запрос Patch на обновление пользователя {}", userDto.toString());
        return userClient.update(userDto, userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable Long userId) {
        log.info("Получен запрос DELETE для пользователя по id {}", userId);
        return userClient.delete(userId);
    }
}