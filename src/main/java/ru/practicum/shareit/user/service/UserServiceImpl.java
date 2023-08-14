package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper mapper) {
        this.repository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public List<UserDto> getUsers() {
        return repository.findAll().stream()
                .map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public User getUserById(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        try {
            return mapper.toUserDto(repository.save(mapper.toUser(userDto)));
        } catch (DataIntegrityViolationException e) {
            throw new UserExistsException("Пользователь с E-mail=" +
                    userDto.getEmail() + " уже существует!");
        }
    }


    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long id) {
        if (userDto.getId() == null) {
            userDto.setId(id);
        }
        User userUpdate = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        if (userDto.getName() != null) {
            userUpdate.setName(userDto.getName());
        }
        if ((userDto.getEmail() != null) && (!userDto.getEmail().equals(userUpdate.getEmail()))) {
            if (repository.findByEmail(userDto.getEmail()).stream()
                    .noneMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
                userUpdate.setEmail(userDto.getEmail());
            } else {
                throw new UserExistsException("Пользователь с E-mail=" + userUpdate.getEmail() + " уже существует!");
            }
        }
        return mapper.toUserDto(repository.save(userUpdate));
    }

    @Override
    public void delete(Long userId) {
        repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        repository.deleteById(userId);
    }
}
