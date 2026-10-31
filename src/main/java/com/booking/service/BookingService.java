package com.booking.service;

import com.booking.dto.BookingRequestDTO;
import com.booking.dto.BookingResponseDTO;
import com.booking.entity.BookingStatus;

import java.util.List;

public interface BookingService {

    BookingResponseDTO createBooking(BookingRequestDTO request);

    List<BookingResponseDTO> getAllBookings();

    BookingResponseDTO getBookingById(Long id);

    BookingResponseDTO updateBooking(Long id, BookingRequestDTO request);

    BookingResponseDTO updateBookingStatus(Long id, BookingStatus status);

    void deleteBooking(Long id);
}

