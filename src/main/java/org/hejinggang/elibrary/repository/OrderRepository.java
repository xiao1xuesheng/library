package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNo(String orderNo);

    Optional<Order> findByRequestId(String requestId);

    Optional<Order> findByPayOrderNo(String payOrderNo);

    List<Order> findByPayTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<Order> findByStatus(String status);

    List<Order> findByUserIdAndStatus(Long userId, String status);
}