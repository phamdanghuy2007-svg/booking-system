package com.booking.controller;

import com.booking.dto.ServiceRequestDTO;
import com.booking.entity.SalonService;
import com.booking.exception.ResourceNotFoundException;
import com.booking.repository.SalonServiceRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final SalonServiceRepository salonServiceRepository;

    public ServiceController(SalonServiceRepository salonServiceRepository) {
        this.salonServiceRepository = salonServiceRepository;
    }

    @GetMapping
    public ResponseEntity<List<SalonService>> getAllServices() {
        return ResponseEntity.ok(salonServiceRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalonService> getServiceById(@PathVariable Long id) {
        SalonService service = salonServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        return ResponseEntity.ok(service);
    }

    @PostMapping
    public ResponseEntity<SalonService> createService(@Valid @RequestBody ServiceRequestDTO request) {
        SalonService service = SalonService.builder()
                .name(request.getName())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .price(request.getPrice())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        SalonService saved = salonServiceRepository.save(service);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalonService> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequestDTO request) {
        SalonService service = salonServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setPrice(request.getPrice());
        if (request.getIsActive() != null) {
            service.setIsActive(request.getIsActive());
        }

        return ResponseEntity.ok(salonServiceRepository.save(service));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        SalonService service = salonServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        salonServiceRepository.delete(service);
        return ResponseEntity.noContent().build();
    }
}
