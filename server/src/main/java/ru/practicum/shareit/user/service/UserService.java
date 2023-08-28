package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers();

    User getUserById(Long id);

    UserDto create(UserDto userDto);

    UserDto update(UserDto userDto, Long id);

    void delete(Long userId);
}