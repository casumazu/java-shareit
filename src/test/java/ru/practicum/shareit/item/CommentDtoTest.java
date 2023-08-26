package ru.practicum.shareit.item;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class CommentDtoTest {

    @Autowired
    private JacksonTester<CommentDto> json;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CommentDto commentDto;
    private final Validator validator = Validation.buildDefaultValidatorFactory()
            .getValidator();

    @BeforeEach
    void setUp() {
        commentDto = new CommentDto(1L, "text", null, LocalDateTime.now());
    }

    @SneakyThrows
    @Test
    void testJson() {
        JsonContent<CommentDto> actual = json.write(commentDto);

        assertThat(actual).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(actual).extractingJsonPathStringValue("$.text").isEqualTo("text");
    }

    @Test
    void commentDtoTextIsNullViolationsNotBlank() {
        commentDto.setText(null);
        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);
        assertThat(violations).isNotEmpty();
    }
}
