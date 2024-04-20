package stevanovic.dejana.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stevanovic.dejana.orderservice.model.Order;

import java.util.List;

public interface OrderRepository  extends JpaRepository<Order, Long> {

    List<Order> findAllByUserId(Long userId);
}
