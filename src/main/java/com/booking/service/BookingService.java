package com.booking.service;

import com.booking.dto.BookingRequestDTO;
import com.booking.dto.BookingResponseDTO;
import com.booking.dto.TimeSlotDTO;
import com.booking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingResponseDTO createBooking(BookingRequestDTO request);

    List<BookingResponseDTO> getAllBookings();

    Page<BookingResponseDTO> getBookingsPaged(BookingStatus status, Long staffId, Pageable pageable);

    BookingResponseDTO getBookingById(Long id);

    BookingResponseDTO updateBooking(Long id, BookingRequestDTO request);

    BookingResponseDTO updateBookingStatus(Long id, BookingStatus status);

    BookingResponseDTO confirmBooking(Long id);

    BookingResponseDTO cancelBooking(Long id);

    BookingResponseDTO completeBooking(Long id);

    void deleteBooking(Long id);

    List<TimeSlotDTO> getAvailableSlots(Long staffId, LocalDate date, Integer durationMinutes);
}
