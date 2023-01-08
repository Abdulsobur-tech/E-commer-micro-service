package com.programmingtechie.anotherservice.repository;

import com.programmingtechie.anotherservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
