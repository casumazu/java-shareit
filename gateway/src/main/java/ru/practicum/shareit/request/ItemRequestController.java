package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import static ru.practicum.shareit.booking.BookingController.HEADER_USER_ID;

@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @ResponseBody
    @PostMapping
    public ResponseEntity<Object> create(@RequestBody @Valid ItemRequestDto itemRequestDto,
                                         @RequestHeader(HEADER_USER_ID) Long requesterId) {
        log.info("Получен запрос POST создание запроса на вещь {}, от пользователя id {}", itemRequestDto, requesterId);
        return itemRequestClient.create(itemRequestDto, requesterId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(@PathVariable("requestId") Long itemRequestId,
                                           @RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Получен GET запрос на просмотр данных о запросе id {}, отправлен пользователем id {}",
                itemRequestId, userId);
        return itemRequestClient.getItemRequestById(userId, itemRequestId);
    }


    @GetMapping
    public ResponseEntity<Object> findAllByOwner(@RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Получен GET запрос на просмотр списка всех своих запросов, отправлен пользователем id {}", userId);
        return itemRequestClient.getOwnItemRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(@RequestHeader(HEADER_USER_ID) Long userId,
                                          @Valid @RequestParam(value = "from", defaultValue = "0")
                                          @Min(0) Integer from,
                                          @Valid @RequestParam(value = "size", defaultValue = "20")
                                          @Min(1) @Max(100) Integer size) {
        log.info("Получен GET запрос на просмотр списка всех запросов созданных другими пользователями," +
                " отправлен пользователем id {}", userId);
        return itemRequestClient.getAllItemRequests(userId, from, size);
    }
}
