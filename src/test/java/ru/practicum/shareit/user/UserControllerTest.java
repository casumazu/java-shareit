package ru.practicum.shareit.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.exception.UserExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserServiceImpl;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserServiceImpl userService;

    private final UserDto userDto = new UserDto(
            1L,
            "Bogdan",
            "sergeev.bog@yandex.ru");

    @SneakyThrows
    @Test
    void saveUser() throws Exception {
        when(userService.create(userDto)).thenReturn(userDto);

        String result = mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(mapper.writeValueAsString(userDto), result);
    }

    @Test
    void saveUserTwo() throws Exception {
        when(userService.create(any()))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @SneakyThrows
    @Test
    void findById() {
        when(userService.getUserById(userDto.getId())).thenReturn(UserMapper.toUser(userDto));

        mvc.perform(get("/users/{id}", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService).getUserById(userDto.getId());
    }

    @SneakyThrows
    @Test
    void findUserByIdWithWrongId() {
        String expectedErrorMessage = "Пользователь не найден";
        when(userService.getUserById(999L)).thenThrow(new UserNotFoundException(expectedErrorMessage));

        mvc.perform(MockMvcRequestBuilders.get("/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals(expectedErrorMessage, result.getResolvedException().getMessage()));
    }

    @Test
    void updateUser() throws Exception {
        when(userService.update(userDto, userDto.getId())).thenReturn(userDto);

        mvc.perform(patch("/users/{id}", userDto.getId())
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService).update(userDto, userDto.getId());
    }

    @Test
    void getUsers() throws Exception {
        UserDto user1 = new UserDto(1L, "User1", "user1@example.com");
        UserDto user2 = new UserDto(2L, "User2", "user2@example.com");
        List<UserDto> users = Arrays.asList(user1, user2);

        when(userService.getUsers()).thenReturn(users);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(users)));
    }

    @Test
    void updateUserWithDuplicateEmail() throws Exception {
        when(userService.update(userDto, userDto.getId())).thenThrow(UserExistsException.class);

        mvc.perform(patch("/users/{id}", userDto.getId())
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        verify(userService).update(userDto, userDto.getId());
    }

    @Test
    void deleteUser() throws Exception {
        mvc.perform(delete("/users/{id}", userDto.getId()))
                .andExpect(status().isOk());

        verify(userService).delete(userDto.getId());
    }
}