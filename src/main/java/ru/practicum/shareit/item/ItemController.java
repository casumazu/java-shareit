package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private static final String OWNER = "X-Sharer-User-Id";
    private final ItemService itemService;
    private final UserService userService;


    @Autowired
    public ItemController(ItemService itemService, UserService userService) {
        this.itemService = itemService;
        this.userService = userService;
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getItemById(@PathVariable Long itemId,  @RequestHeader(OWNER) Long ownerId) {
        log.info("Получен GET-запрос на получение вещи {}", itemId);
        return itemService.getItemById(itemId, ownerId);
    }

    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto, @RequestHeader(OWNER) Long ownerId) {
        log.info("Получен POST-запрос на добавление вещи владельцем {}", ownerId);
        ItemDto newItemDto = null;
        if (userService.getUserById(ownerId) != null) {
            newItemDto = itemService.create(itemDto, ownerId);
        }
        return newItemDto;
    }

    @GetMapping
    public List<ItemWithBookingsDto> getItemsByOwner(@RequestHeader(OWNER) Long ownerId) {
        log.info("Получен GET-запрос на получение всех вещей владельца {}", ownerId);
        return itemService.getItemsByOwnerId(ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto, @PathVariable Long itemId,
                          @RequestHeader(OWNER) Long ownerId) {
        log.info("Получен PATCH-запрос на обновление вещи {}", itemId);
        return itemService.update(itemDto, itemId, ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsBySearchQuery(
            @RequestParam String text,
            @Valid @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @Valid @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(20) Integer size) {
        log.info("Получен GET-запрос на поиск вещи с текстом = {}", text);
        return itemService.getItemsBySearchQuery(text, PageRequest.of(from / size, size));
    }

    @PostMapping("/{itemId}/comment")
    CommentDto addComment(@RequestHeader(OWNER) long userId,
                          @Valid @RequestBody CommentDto commentDto,
                          @PathVariable long itemId) {
        log.info("Получен запрос POST на добавление комментария {} к вещи id {}, пользователем id {}",
                commentDto, itemId, userId);
        return itemService.createComment(commentDto, itemId, userId);
    }
}