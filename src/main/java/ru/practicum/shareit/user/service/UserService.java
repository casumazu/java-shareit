package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final UserMapper mapper;
    private final ItemService itemService;

    @Autowired
    public UserService(UserStorage userStorage, UserMapper userMapper, ItemService itemService) {
        this.userStorage = userStorage;
        this.mapper = userMapper;
        this.itemService = itemService;
    }

    public List<UserDto> getUsers() {
        return userStorage.getUsers().stream()
                .map(mapper::toUserDto)
                .collect(toList());
    }

    public UserDto getUserById(Long id) {
        return mapper.toUserDto(userStorage.getUserById(id));
    }

    public UserDto create(UserDto userDto) {
        return mapper.toUserDto(userStorage.create(mapper.toUser(userDto)));
    }

    public UserDto update(UserDto userDto, Long id) {
        if (userDto.getId() == null) {
            userDto.setId(id);
        }
        return mapper.toUserDto(userStorage.update(mapper.toUser(userDto)));
    }

    public UserDto delete(Long userId) {
        itemService.deleteItemsByOwnerId(userId);
        return mapper.toUserDto(userStorage.delete(userId));
    }
}