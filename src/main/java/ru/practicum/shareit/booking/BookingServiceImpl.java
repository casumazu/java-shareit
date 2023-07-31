package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;


    @Autowired
    public BookingServiceImpl(UserRepository userRepository, ItemRepository itemRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public BookingDto add(Long userId, BookingInputDto bookingInputDto) {
        if (bookingInputDto.getStart().equals(bookingInputDto.getEnd()) ||
                bookingInputDto.getStart().isAfter(bookingInputDto.getEnd())) {
            log.info("Ошибка в выборе даты бронирования, дата старта {}, дата окончания {}",
                    bookingInputDto.getStart(), bookingInputDto.getEnd());
            throw new ValidationException("Бронирование недоступно, ошибка в выборе даты бронирования");
        }

        Booking booking = BookingMapper.toBooking(bookingInputDto);
        booking.setItem(getItem(bookingInputDto.getItemId()));
        booking.setBooker(getUser(userId));

        if (userId.equals(booking.getItem().getOwner().getId())) {
            log.info("Пользователь {} является владельцем вещи {} и не может ее забронировать",
                    userId, booking.getItem());
            throw new BookingNotFoundException("Недоступна для бронирования владельцем");
        }

        if (!booking.getItem().getAvailable()) {
            log.info("Вещь {} недоступна для бронирования", booking.getItem());
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        BookingDto bookingDto = BookingMapper.toBookingDto(bookingRepository.save(booking));
        log.info("Добавлено новое бронирование {}", bookingDto);
        return bookingDto;
    }

    @Override
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = getBooking(bookingId, userId);
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new ValidationException("Недоступно, статус уже был изменен");
        }
        if (!Objects.equals(booking.getBooker().getId(), userId)) {
            if (approved) {
                booking.setStatus(Status.APPROVED);
            } else {
                booking.setStatus(Status.REJECTED);
            }
        } else {
            if (!approved) {
                booking.setStatus(Status.CANCELED);
            } else {
                throw new BookingNotFoundException("Подтвердить бронирование может только владелец вещи");
            }
        }
        BookingDto bookingDto = BookingMapper.toBookingDto(bookingRepository.save(booking));
        log.info("Статус бронирования изменился {}", bookingDto);
        return bookingDto;
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        BookingDto bookingDto = BookingMapper.toBookingDto(getBooking(bookingId, userId));
        log.info("Запрошена информация о бронировании по id {}, пользователем {}",
                bookingId, userId);
        return bookingDto;
    }

    @Override
    public List<BookingDto> getAllBookings(Long userId, String state) {
        getUser(userId);
        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findAllByBooker_IdOrderByStartDesc(userId);
                break;
            case "CURRENT":
                bookings = bookingRepository.findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now());
                break;
            case "FUTURE":
                bookings = bookingRepository.findAllByBooker_IdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case "PAST":
                bookings = bookingRepository.findAllByBooker_IdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case "WAITING":
                bookings = bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
            default:
                throw new UnknownStatusException("Unknown state: " + state);
        }
        List<BookingDto> bookingDto = bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
        log.info("Результат на запрос всех бронирований пользователем {} {}", userId, bookingDto);
        return bookingDto;
    }

    @Override
    public List<BookingDto> getAllBookingsForOwner(Long userId, String state) {
        getUser(userId);
        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(userId);
                break;
            case "CURRENT":
                bookings = bookingRepository.findAllByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, LocalDateTime.now(), LocalDateTime.now());
                break;
            case "FUTURE":
                bookings = bookingRepository.findAllByItem_Owner_IdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case "PAST":
                bookings = bookingRepository.findAllByItem_Owner_IdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case "WAITING":
                bookings = bookingRepository.findAllByItem_Owner_IdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findAllByItem_Owner_IdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
            default:
                throw new UnknownStatusException("Unknown state: " + state);
        }
        List<BookingDto> bookingDto = bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
        log.info("Результат на запрос всех бронирований вещей владельцем {} {}", userId, bookingDto);
        return bookingDto;
    }


    private Booking getBooking(Long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено"));
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            log.info("Просмотр бронирования не доступен пользователю {}", userId);
            throw new BookingNotFoundException("Пользователь не является автором бронирования, либо владельцем вещи");
        }
        return booking;
    }

    private Item getItem(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }
}
