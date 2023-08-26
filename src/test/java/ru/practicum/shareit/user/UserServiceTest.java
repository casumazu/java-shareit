package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTest {
    private final UserService userService;
    private UserDto userDto;
    private UserDto updateUser;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("Bogdan")
                .email("userDto@yandex.ru")
                .build();
    }

    @Test
    void addUser() {
        userService.create(userDto);
        assertEquals(userDto.getName(), "Bogdan");
        assertEquals(userDto.getEmail(), "userDto@yandex.ru");
    }

    @Test
    void testUpdateUser() {
        UserDto createUser = UserDto.builder()
                .name("create")
                .email("create@yandex.ru")
                .build();
        UserDto createdUser = userService.create(createUser);

        updateUser = UserDto.builder()
                .id(createdUser.getId())
                .name("update")
                .email("update@yandex.ru")
                .build();

        userService.update(updateUser, createdUser.getId());

        User updatedUser = userService.getUserById(createdUser.getId());
        assertEquals(updatedUser.getName(), "update");
        assertEquals(updatedUser.getEmail(), "update@yandex.ru");
    }

    @Test
    void deleteUser() {
        User user = new User(5L, "Bog", "Bog@yandex.ru");
        UserDto userDto = userService.create(UserMapper.toUserDto(user));
        List<UserDto> users = userService.getUsers();
        int size = users.size();
        userService.delete(userDto.getId());
        users = userService.getUsers();
        assertThat(users.size(), equalTo(size - 1));
    }

    @Test
    void exceptionWhenDeleteUserWithWrongId() {
        UserNotFoundException e = assertThrows(UserNotFoundException.class, () -> userService.delete(999L));
        assertEquals("Пользователь не найден", e.getMessage());
    }

}