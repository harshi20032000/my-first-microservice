package com.ms.booking.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ms.booking.entity.Passenger;

public interface PassengerRepository extends JpaRepository<Passenger, Integer> {

}
