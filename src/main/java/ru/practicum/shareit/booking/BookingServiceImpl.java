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
        LocalDateTime start = bookingInputDto.getStart();
        LocalDateTime end = bookingInputDto.getEnd();

        if (start.equals(end) || start.isAfter(end)) {
            String errorMessage = String.format("Ошибка в выборе даты бронирования," +
                    " дата старта %s, дата окончания %s", start, end);
            log.info(errorMessage);
            throw new ValidationException("Бронирование недоступно, " +
                    "ошибка в выборе даты бронирования: " + errorMessage);
        }

        Item item = getItem(bookingInputDto.getItemId());

        if (userId.equals(item.getOwner().getId())) {
            String errorMessage = String.format("Пользователь %d является " +
                    "владельцем вещи %s и не может ее забронировать", userId, item);
            log.info(errorMessage);
            throw new BookingNotFoundException("Недоступна для бронирования владельцем: " + errorMessage);
        }

        if (!item.getAvailable()) {
            String errorMessage = String.format("Вещь %s недоступна для бронирования", item);
            log.info(errorMessage);
            throw new ValidationException("Вещь недоступна для бронирования: " + errorMessage);
        }

        Booking booking = BookingMapper.toBooking(bookingInputDto);
        booking.setItem(item);
        booking.setBooker(getUser(userId));

        Booking savedBooking = bookingRepository.save(booking);
        BookingDto bookingDto = BookingMapper.toBookingDto(savedBooking);
        log.info("Добавлено новое бронирование {}", bookingDto);
        return bookingDto;
    }

    @Override
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = getBooking(bookingId, userId);

        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Недоступно, статус уже был изменен");
        }

        if (booking.getBooker().getId().equals(userId)) {
            if (approved) {
                throw new BookingNotFoundException("Подтвердить бронирование может только владелец вещи");
            } else {
                booking.setStatus(Status.CANCELED);
            }
        } else {
            booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
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


    private List<Booking> getBookingsByStateForUser(Long userId, String state) {
        switch (state) {
            case "ALL":
                return bookingRepository.findAllByBooker_IdOrderByStartDesc(userId);
            case "CURRENT":
                return bookingRepository.findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, LocalDateTime.now(), LocalDateTime.now());
            case "FUTURE":
                return bookingRepository.findAllByBooker_IdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
            case "PAST":
                return bookingRepository.findAllByBooker_IdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case "WAITING":
                return bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId, Status.WAITING);
            case "REJECTED":
                return bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId, Status.REJECTED);
            default:
                throw new UnknownStatusException("Unknown state: " + state);
        }
    }


    private List<Booking> getBookingsByStateForOwner(Long userId, String state) {
        switch (state) {
            case "ALL":
                return bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(userId);
            case "CURRENT":
                return bookingRepository.findAllByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, LocalDateTime.now(), LocalDateTime.now());
            case "FUTURE":
                return bookingRepository.findAllByItem_Owner_IdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
            case "PAST":
                return bookingRepository.findAllByItem_Owner_IdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case "WAITING":
                return bookingRepository.findAllByItem_Owner_IdAndStatusOrderByStartDesc(userId, Status.WAITING);
            case "REJECTED":
                return bookingRepository.findAllByItem_Owner_IdAndStatusOrderByStartDesc(userId, Status.REJECTED);
            default:
                throw new UnknownStatusException("Unknown state: " + state);
        }
    }

    @Override
    public List<BookingDto> getAllBookings(Long userId, String state) {
        getUser(userId);
        List<Booking> bookings = getBookingsByStateForUser(userId, state);
        List<BookingDto> bookingDto = bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
        log.info("Результат на запрос всех бронирований пользователем {} {}", userId, bookingDto);
        return bookingDto;
    }

    @Override
    public List<BookingDto> getAllBookingsForOwner(Long userId, String state) {
        getUser(userId);
        List<Booking> bookings = getBookingsByStateForOwner(userId, state);
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
