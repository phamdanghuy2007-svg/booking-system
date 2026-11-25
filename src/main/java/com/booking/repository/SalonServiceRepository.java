package com.booking.repository;

import com.booking.entity.SalonService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalonServiceRepository extends JpaRepository<SalonService, Long> {

    Optional<SalonService> findByName(String name);
}

