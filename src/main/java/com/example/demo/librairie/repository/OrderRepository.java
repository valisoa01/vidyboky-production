package com.example.demo.librairie.repository;

import com.example.demo.librairie.entity.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
  List<Order> findByCustomerId(UUID customerId);
    @Query(
            """
            select distinct o from Order o
            left join fetch o.customer
            left join fetch o.payment
            left join fetch o.lines l
            left join fetch l.bookFormat bf
            left join fetch bf.book
            left join fetch bf.format
            where o.id = :id
            """)
    Optional<Order> findDetailedById(UUID id);
}
