package ru.practicum.shareit.item;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.booking.BookingController.HEADER_USER_ID;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> findAllByOwner(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                                 @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                 @RequestParam(required = false) Integer size) {
        log.info("Получен GET запрос на просмотр вещей, отправлен пользователем id {}", ownerId);
        return itemClient.getItemsByOwner(ownerId, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(HEADER_USER_ID) Long userId,
                                         @RequestBody @Valid ItemDto itemDto) {
        log.info("Получен запрос POST на добавление вещи {}, пользователем id {}", itemDto, userId);
        return itemClient.create(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findById(@RequestHeader(HEADER_USER_ID) Long userId,
                                           @PathVariable Long itemId) {
        log.info("Получен GET запрос на просмотр вещи id {}, отправлен пользователем id {}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    @ResponseBody
    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestBody ItemDto itemDto, @PathVariable Long itemId,
                                         @RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Получен запрос PATCH запрос на обновление вещи id {}, пользователем id {}", itemId, userId);
        return itemClient.update(itemDto, itemId, userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text,
                                              @Valid @RequestParam(value = "from", defaultValue = "0")
                                              @Min(0) Integer from,
                                              @Valid @RequestParam(value = "size", defaultValue = "20")
                                              @Min(1) @Max(100) Integer size) {
        log.info("Получен GET запрос на поиск вещи по {}", text);
        return itemClient.searchItems(text, from, size);
    }

    @ResponseBody
    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestBody @Valid CommentDto commentDto,
                                                @RequestHeader(HEADER_USER_ID) Long userId,
                                                @PathVariable Long itemId) {
        log.info("Получен запрос POST на добавление комментария {} к вещи id {}, пользователем id {}",
                commentDto, itemId, userId);
        return itemClient.createComment(commentDto, itemId, userId);
    }
}