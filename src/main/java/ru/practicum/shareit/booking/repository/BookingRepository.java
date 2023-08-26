package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByBookerId(Long bookerId, Pageable pageable);

    Page<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start,
                                                              LocalDateTime end, Pageable pageable);

    Page<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    Page<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    Page<Booking> findByBookerIdAndStatus(Long bookerId, Status status, Pageable pageable);

    Page<Booking> findByItem_Owner_Id(Long ownerId, Pageable pageable);

    Page<Booking> findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime start,
                                                                   LocalDateTime end, Pageable pageable);

    Page<Booking> findByItem_Owner_IdAndEndIsBefore(Long ownerId, LocalDateTime end, Pageable pageable);

    Page<Booking> findByItem_Owner_IdAndStartIsAfter(Long ownerId, LocalDateTime start, Pageable pageable);

    Page<Booking> findByItem_Owner_IdAndStatus(Long ownerId, Status status, Pageable pageable);

    Booking findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(Long itemId,
                                                                    LocalDateTime end, Status status);

    Booking findFirstByItem_IdAndStartBeforeOrderByEndDesc(Long itemId, LocalDateTime end);

    Booking findFirstByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(Long itemId, Long userId,
                                                                  LocalDateTime end, Status status);
}
