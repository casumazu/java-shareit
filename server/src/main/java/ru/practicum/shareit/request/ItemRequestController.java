package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private static final String OWNER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addRequest(@RequestBody ItemRequestDto itemRequestDto,
                                     @RequestHeader(OWNER) Long requestId) {
        log.info("Получен POST-запрос на создание запроса вещи от пользователя {}", requestId);
        return itemRequestService.add(itemRequestDto, requestId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(OWNER) Long userId, @PathVariable Long requestId) {
        log.info("Получен GET-запрос на получение запросов user{} от user{}", requestId, userId);
        return itemRequestService.findById(userId, requestId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllRequestsByOwnerId(@RequestHeader(OWNER) Long userId) {
        log.info("Получен GET-запрос на получение всех запросов пользователя {}", userId);
        return itemRequestService.findAllByOwnerId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(OWNER) Long userId,
                                       @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                       @RequestParam(value = "size", defaultValue = "20")
                                       @Min(1) @Max(100) Integer size) {
        return itemRequestService.findAll(userId, PageRequest.of(from / size, size));
    }
}
