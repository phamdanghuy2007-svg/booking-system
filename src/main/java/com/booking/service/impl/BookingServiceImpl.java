package com.booking.service.impl;

import com.booking.dto.BookingRequestDTO;
import com.booking.dto.BookingResponseDTO;
import com.booking.dto.TimeSlotDTO;
import com.booking.entity.Booking;
import com.booking.entity.BookingStatus;
import com.booking.entity.SalonService;
import com.booking.entity.User;
import com.booking.exception.ResourceNotFoundException;
import com.booking.repository.BookingRepository;
import com.booking.repository.SalonServiceRepository;
import com.booking.repository.UserRepository;
import com.booking.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SalonServiceRepository salonServiceRepository;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            SalonServiceRepository salonServiceRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.salonServiceRepository = salonServiceRepository;
    }

    @Override
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        // 1. Resolve Customer ID
        Long customerId = request.getCustomerId();
        if (customerId == null) {
            Optional<User> userOpt = userRepository.findByEmail(request.getCustomerEmail());
            if (userOpt.isPresent()) {
                customerId = userOpt.get().getId();
            } else {
                User newCustomer = User.builder()
                        .fullName(request.getCustomerName())
                        .email(request.getCustomerEmail())
                        .phone(request.getCustomerPhone())
                        .passwordHash("NO_PASSWORD")
                        .role("CUSTOMER")
                        .isActive(true)
                        .build();
                customerId = userRepository.save(newCustomer).getId();
            }
        }

        // 2. Resolve Service ID
        Long serviceId = request.getServiceId();
        SalonService salonService = null;
        if (serviceId != null) {
            salonService = salonServiceRepository.findById(serviceId).orElse(null);
        }
        if (salonService == null && request.getServiceName() != null) {
            salonService = salonServiceRepository.findByName(request.getServiceName()).orElse(null);
        }
        if (salonService != null) {
            serviceId = salonService.getId();
        } else {
            serviceId = salonServiceRepository.findAll().stream()
                    .findFirst()
                    .map(SalonService::getId)
                    .orElse(1L);
        }

        // 3. Resolve Total Price
        BigDecimal totalPrice = request.getTotalPrice();
        if (totalPrice == null) {
            totalPrice = (salonService != null && salonService.getPrice() != null)
                    ? salonService.getPrice()
                    : BigDecimal.valueOf(80000);
        }

        // 4. Resolve End Time (Ensure endTime > startTime to satisfy CK_booking_time)
        LocalDateTime endTime = request.getEndTime();
        if (endTime == null || !endTime.isAfter(request.getStartTime())) {
            int duration = (salonService != null && salonService.getDurationMinutes() != null)
                    ? salonService.getDurationMinutes()
                    : 30;
            endTime = request.getStartTime().plusMinutes(duration);
        }

        // 5. Resolve Staff ID
        Long staffId = request.getStaffId();
        if (staffId == null) {
            staffId = 2L; // Default staff (e.g. Nguyễn Văn Thợ 1)
        }

        // 6. Double-booking / Conflict check
        boolean isBusy = bookingRepository.isStaffBusy(
                staffId,
                request.getStartTime(),
                endTime,
                BookingStatus.CANCELLED
        );

        if (isBusy) {
            throw new IllegalArgumentException("Thợ đã có lịch bận trong khung giờ này ("
                    + request.getStartTime() + " đến " + endTime + "), vui lòng chọn giờ hoặc thợ khác!");
        }

        Booking booking = Booking.builder()
                .customerId(customerId)
                .serviceId(serviceId)
                .staffId(staffId)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .serviceName(salonService != null ? salonService.getName() : request.getServiceName())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .totalPrice(totalPrice)
                .note(request.getNote())
                .status(BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDTO> getBookingsPaged(BookingStatus status, Long staffId, Pageable pageable) {
        Page<Booking> page;
        if (status != null && staffId != null) {
            page = bookingRepository.findByStaffIdAndStatus(staffId, status, pageable);
        } else if (status != null) {
            page = bookingRepository.findByStatus(status, pageable);
        } else if (staffId != null) {
            page = bookingRepository.findByStaffId(staffId, pageable);
        } else {
            page = bookingRepository.findAll(pageable);
        }
        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return mapToResponse(booking);
    }

    @Override
    public BookingResponseDTO updateBooking(Long id, BookingRequestDTO request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        LocalDateTime newStartTime = request.getStartTime();
        LocalDateTime newEndTime = request.getEndTime() != null ? request.getEndTime() : booking.getEndTime();
        Long staffId = request.getStaffId() != null ? request.getStaffId() : booking.getStaffId();

        // Conflict check excluding current booking
        boolean isBusy = bookingRepository.isStaffBusyExcluding(
                staffId,
                id,
                newStartTime,
                newEndTime,
                BookingStatus.CANCELLED
        );
        if (isBusy) {
            throw new IllegalArgumentException("Thợ đã có lịch bận trong khung giờ mới ("
                    + newStartTime + " đến " + newEndTime + "), vui lòng chọn giờ khác!");
        }

        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerEmail(request.getCustomerEmail());
        booking.setCustomerPhone(request.getCustomerPhone());
        booking.setServiceName(request.getServiceName());
        booking.setStartTime(newStartTime);
        booking.setEndTime(newEndTime);
        if (request.getTotalPrice() != null) {
            booking.setTotalPrice(request.getTotalPrice());
        }
        booking.setNote(request.getNote());
        booking.setStaffId(staffId);

        Booking updatedBooking = bookingRepository.save(booking);
        return mapToResponse(updatedBooking);
    }

    @Override
    public BookingResponseDTO updateBookingStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        booking.setStatus(status);
        Booking updatedBooking = bookingRepository.save(booking);
        return mapToResponse(updatedBooking);
    }

    @Override
    public BookingResponseDTO confirmBooking(Long id) {
        return updateBookingStatus(id, BookingStatus.CONFIRMED);
    }

    @Override
    public BookingResponseDTO cancelBooking(Long id) {
        return updateBookingStatus(id, BookingStatus.CANCELLED);
    }

    @Override
    public BookingResponseDTO completeBooking(Long id) {
        return updateBookingStatus(id, BookingStatus.COMPLETED);
    }

    @Override
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        bookingRepository.delete(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotDTO> getAvailableSlots(Long staffId, LocalDate date, Integer durationMinutes) {
        if (staffId == null) {
            staffId = 2L; // Default staff
        }
        if (date == null) {
            date = LocalDate.now();
        }
        int duration = (durationMinutes != null && durationMinutes > 0) ? durationMinutes : 30;

        // Salon operating hours: 08:00 to 20:00
        LocalTime openingTime = LocalTime.of(8, 0);
        LocalTime closingTime = LocalTime.of(20, 0);

        // Fetch bookings for this staff on this date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<Booking> dayBookings = bookingRepository.findByStaffIdAndStartTimeBetween(staffId, startOfDay, endOfDay)
                .stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .collect(Collectors.toList());

        List<TimeSlotDTO> slots = new ArrayList<>();
        LocalTime current = openingTime;

        while (current.plusMinutes(duration).isBefore(closingTime) || current.plusMinutes(duration).equals(closingTime)) {
            LocalTime slotStart = current;
            LocalTime slotEnd = current.plusMinutes(duration);

            // Check if slot overlaps with any existing booking
            boolean busy = dayBookings.stream().anyMatch(b -> {
                LocalTime bStart = b.getStartTime().toLocalTime();
                LocalTime bEnd = b.getEndTime().toLocalTime();
                return bStart.isBefore(slotEnd) && bEnd.isAfter(slotStart);
            });

            slots.add(TimeSlotDTO.builder()
                    .startTime(slotStart)
                    .endTime(slotEnd)
                    .available(!busy)
                    .build());

            current = current.plusMinutes(duration);
        }

        return slots;
    }

    private BookingResponseDTO mapToResponse(Booking booking) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .customerId(booking.getCustomerId())
                .serviceId(booking.getServiceId())
                .staffId(booking.getStaffId())
                .customerName(booking.getCustomerName())
                .customerEmail(booking.getCustomerEmail())
                .customerPhone(booking.getCustomerPhone())
                .serviceName(booking.getServiceName())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .note(booking.getNote())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
