package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final RequestRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;


    @Override
    @Transactional
    public ItemRequestDto add(ItemRequestDto itemRequestDto, Long requestId) {
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequester(getUser(requestId));
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setCreated(LocalDateTime.now());
        log.info("Создан запрос на вещь от user{}", requestId);
        return ItemRequestMapper.toItemRequestDto(repository.save(itemRequest));
    }

    @Override
    public ItemRequestDto findById(Long userId, Long requestId) {
        getUser(userId);
        ItemRequest itemRequest = getRequest(requestId);
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setItems(getItemsDtoByRequestId(requestId));
        log.info("Получены запросы для пользователя {} на запрос {}", userId, requestId);
        return itemRequestDto;
    }

    @Override
    public List<ItemRequestDto> findAll(Long userId, PageRequest pageRequest) {
        getUser(userId);
        List<ItemRequestDto> requestDtos = repository.findItemRequestsByRequester_IdIsNotOrderByCreated(userId,pageRequest)
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
        requestDtos.forEach(itemRequestDto ->  itemRequestDto.setItems(getItemsDtoByRequestId(itemRequestDto.getId())));
        log.info("Получен запрос на получение всех запросов от user {}", userId);
        return requestDtos;
    }

    @Override
    public List<ItemRequestDto> findAllByOwnerId(Long ownerId) {
        getUser(ownerId);
        List<ItemRequestDto> req = repository.findItemRequestsByRequester_IdOrderByCreated(ownerId)
                .stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        req.forEach(itemRequestDto -> itemRequestDto.setItems(getItemsDtoByRequestId(itemRequestDto.getId())));
        log.info("Получен список запросов user {}", ownerId);
        return req;
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    private ItemRequest getRequest(Long id){
        return repository.findById(id).orElseThrow(() -> new RequestNotFoundException("Запрос на вещь не найден"));
    }

    private List<ItemDto> getItemsDtoByRequestId(long requestId) {
        return itemRepository.findItemsByRequestId(requestId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
