package ru.practicum.shareit.requests;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemRequestDtoTest {
    private final LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private final ItemRequestDto itemRequestDto = new ItemRequestDto(1L,
            "description", dateTime, List.of());
    private final Validator validator = Validation.buildDefaultValidatorFactory()
            .getValidator();


    @Test
    void whenItemRequestDtoDescriptionIsBlankThenViolationsShouldNotBlank() {
        itemRequestDto.setDescription(" ");
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(itemRequestDto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenItemRequestDtoDescriptionIsNullThenViolationsShouldNotBlank() {
        itemRequestDto.setDescription(null);
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(itemRequestDto);
        assertThat(violations).isNotEmpty();
    }
}