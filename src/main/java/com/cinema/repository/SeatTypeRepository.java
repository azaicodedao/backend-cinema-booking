package com.cinema.repository;

import com.cinema.entity.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatTypeRepository extends JpaRepository<SeatType, Integer> {
    Optional<SeatType> findByName(String name);
}
