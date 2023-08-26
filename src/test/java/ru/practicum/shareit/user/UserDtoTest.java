package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class UserDtoTest {
    @Autowired
    private JacksonTester<UserDto> jsonUserDto;

    @Test
    void testUserDto() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("Bogdan")
                .email("sergeev.bog@yandex.ru")
                .build();


        JsonContent<UserDto> result = jsonUserDto.write(userDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Bogdan");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("sergeev.bog@yandex.ru");
    }
}
