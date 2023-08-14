package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Validated
public class ItemRequestDto {
    private Long id;
    @NotBlank(message = "Описание не должно быть пустым")
    private String description;

    private LocalDateTime created;

    private List<ItemDto> items;
}
