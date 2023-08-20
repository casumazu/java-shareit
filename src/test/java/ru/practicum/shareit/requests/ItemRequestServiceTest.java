package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {

    private final ItemRequestService itemRequestService;

    private final UserService userService;
    private UserDto userDto1 = new UserDto(54L, "Bogdan", "Bogdan@yandex.ru");
    private UserDto userDto2 = new UserDto(52L, "Bogdan", "Bogdan@yandex.ru");

    private ItemRequestDto itemRequestDto = new ItemRequestDto(100L, "description",
            LocalDateTime.of(2023, 1, 2, 3, 4, 5), null);

    @Test
    void addItemRequest() {
        UserDto newUserDto = userService.create(userDto1);
        ItemRequestDto returnRequestDto = itemRequestService.add(itemRequestDto, newUserDto.getId());
        assertThat(returnRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void exceptionAddItemRequestWithWrongUserId() {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.add(itemRequestDto, -2L));
        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void shouldExceptionWhenGetItemRequestWithWrongId() {
        UserDto firstUserDto = userService.create(userDto1);
        UserNotFoundException exp = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.findById(-2L, firstUserDto.getId()));
        assertEquals("Пользователь не найден", exp.getMessage());
    }

    @Test
    void returnOwnerItemRequests() {
        UserDto newUserDto = userService.create(userDto2);
        ItemRequestDto oneRequestDto = itemRequestService.add(itemRequestDto, newUserDto.getId());
        ItemRequestDto twoRequestDto = itemRequestService.add(itemRequestDto, newUserDto.getId());
        List<ItemRequestDto> listItemRequest = itemRequestService.findAllByOwnerId(newUserDto.getId());
        System.out.println(listItemRequest.toString());
        assertThat(listItemRequest.size(), equalTo(2));
    }

    @Test
    void returnItemRequestById() {
        UserDto firstUserDto = userService.create(userDto1);
        ItemRequestDto newItemRequestDto = itemRequestService.add(itemRequestDto, firstUserDto.getId());
        ItemRequestDto returnItemRequestDto = itemRequestService.findById(
                firstUserDto.getId(), newItemRequestDto.getId());
        assertThat(returnItemRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }
}