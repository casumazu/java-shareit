package ru.practicum.shareit.booking.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

@Component
public class BookingMapper {

    public static Booking toBooking(BookingInputDto bookingInputDto) {
        return new Booking(
                null,
                bookingInputDto.getStart(),
                bookingInputDto.getEnd(),
                null,
                null,
                Status.WAITING
        );
    }

    public static BookingDto toBookingDto(Booking booking) {
        if (booking != null) {
            return new BookingDto(
                    booking.getId(),
                    booking.getStart(),
                    booking.getEnd(),
                    ItemMapper.toItemDto(booking.getItem()),
                    UserMapper.toUserDto(booking.getBooker()),
                    booking.getStatus()
            );
        } else {
            return null;
        }
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        if (booking != null) {
            return new BookingShortDto(
                    booking.getId(),
                    booking.getBooker().getId(),
                    booking.getStart(),
                    booking.getEnd()
            );
        } else {
            return null;
        }
    }
}
