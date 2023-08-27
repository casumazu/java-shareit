package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;


    public ItemServiceImpl(ItemRepository itemRepository, CommentRepository commentRepository,
                           BookingRepository bookingRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }


    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long ownerId) {
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(getUser(ownerId));
        ItemDto itemDtoActual = ItemMapper.toItemDto(itemRepository.save(item));
        log.info("Добавлена новая вещь {}", itemDtoActual);
        return itemDtoActual;
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long itemId, Long userId) {
        Item item = getItem(itemId);

        if (!Objects.equals(item.getOwner().getId(), userId)) {
            log.info("Пользователь {} не является владельцем вещи {}", userId, itemDto);
            throw new UserNotFoundException("Пользователь не является владельцем вещи");
        }
        Optional.ofNullable(itemDto.getName()).ifPresent(item::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(item::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(item::setAvailable);

        ItemDto itemDtoActual = ItemMapper.toItemDto(itemRepository.save(item));
        log.info("Вещь обновлена {}", itemId);
        return itemDtoActual;
    }

    @Override
    public List<ItemWithBookingsDto> getItemsByOwnerId(Long ownerId) {
        log.info("Запрос списка всех вещей пользователя ID{}", ownerId);
        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toFullItemDto)
                .sorted(Comparator.comparing(ItemWithBookingsDto::getId))
                .peek(this::fillBookingsAndComments)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsBySearchQuery(String text, PageRequest pageRequest) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<ItemDto> searchResult = itemRepository.search(text, pageRequest).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        log.info("Поиск вещи по тексту ' {} '", text);
        return searchResult;
    }

    @Override
    public ItemWithBookingsDto getItemById(Long itemId, Long userId) {
        Item item = getItem(itemId);
        ItemWithBookingsDto itemFullDto = ItemMapper.toFullItemDto(item);
        if (item.getOwner().getId().equals(userId)) {
            fillBookingsAndComments(itemFullDto);
            log.info("Запрос вещи по ID{}, выполнен ее владельцем", itemId);
            return itemFullDto;
        }
        itemFullDto.setComments(findAllByItemId(itemId));
        log.info("Запрос вещи по ID{}", itemId);
        return itemFullDto;
    }

    private void fillBookingsAndComments(ItemWithBookingsDto itemFullDto) {
        itemFullDto.setLastBooking(BookingMapper.toBookingShortDto(
                bookingRepository.findFirstByItem_IdAndStartBeforeOrderByEndDesc(itemFullDto.getId(),
                        LocalDateTime.now())));
        itemFullDto.setNextBooking(BookingMapper.toBookingShortDto(
                bookingRepository.findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(itemFullDto.getId(),
                        LocalDateTime.now(), Status.APPROVED)));
        itemFullDto.setComments(findAllByItemId(itemFullDto.getId()));
    }

    private Item getItem(long id) {
        return itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));
    }

    private User getUser(long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    private Booking getBookingWithUserBookedItem(long itemId, long userId) {
        return bookingRepository.findFirstByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(itemId,
                userId, LocalDateTime.now(), Status.APPROVED);
    }

    private List<CommentDto> findAllByItemId(long itemId) {
        return commentRepository.findByItemId(itemId).stream()
                .map(ItemMapper::commentDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(CommentDto commentDto, Long itemId, Long userId) {
        Booking booking = getBookingWithUserBookedItem(itemId, userId);
        if (booking == null) {
            log.info("Ошибка добавления отзыва, пользователь ID{} не арендовал данную вещь {}", userId, itemId);
            throw new ValidationException("Ошибка добавления отзыва, пользователь не брал в аренду данную вещь");
        }
        Comment comment = ItemMapper.toComment(commentDto);
        comment.setItem(booking.getItem());
        comment.setAuthor(booking.getBooker());
        comment.setCreated(LocalDateTime.now());
        CommentDto commentDtoNew = ItemMapper.commentDto(commentRepository.save(comment));
        log.info("Добавлен новый отзыв {} к вещи с ID{}", commentDtoNew, itemId);
        return commentDtoNew;
    }
}