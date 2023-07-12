package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.UserExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    public Map<Long, User> users;
    private Long id;

    public InMemoryUserStorage() {
        id = 0L;
        users = new HashMap<>();
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        if (users.values().stream().noneMatch(u -> u.getEmail().equals(user.getEmail()))) {
            if (isValidUser(user)) {
                user.setId(++id);
                users.put(user.getId(), user);
            }
        } else {
            throw new UserExistsException("Пользователь с E-mail = " + user.getEmail() + " уже существует");
        }
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Не передан ID пользователя");
        }
        if (!users.containsKey(user.getId())) {
            throw new UserNotFoundException("Пользователь c ID " + user.getId() + " не найден");
        }
        if (user.getName() == null) {
            user.setName(users.get(user.getId()).getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(users.get(user.getId()).getEmail());
        }
        if (users.values().stream()
                .filter(u -> u.getEmail().equals(user.getEmail()))
                .allMatch(u -> u.getId().equals(user.getId()))) {
            if (isValidUser(user)) {
                users.put(user.getId(), user);
            }
        } else {
            throw new UserExistsException("Пользователь с E-mail - " + user.getEmail() + " уже существует");
        }
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден!");
        }
        return users.get(userId);
    }

    @Override
    public User delete(Long userId) {
        if (userId == null) {
            throw new ValidationException("Не передан userId");
        }
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден!");
        }
        return users.remove(userId);
    }

    private boolean isValidUser(User user) {
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный e-mail пользователя: " + user.getEmail());
        }
        if ((user.getName().isEmpty()) || (user.getName().indexOf(' ') != -1)) {
            throw new ValidationException("Некорректный логин пользователя: " + user.getName());
        }
        return true;
    }
}