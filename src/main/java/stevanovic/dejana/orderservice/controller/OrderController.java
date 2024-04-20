package stevanovic.dejana.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import stevanovic.dejana.orderservice.dto.CreateOrderRequest;
import stevanovic.dejana.orderservice.dto.GetOrdersRequest;
import stevanovic.dejana.orderservice.dto.UpdateOrderRequest;
import stevanovic.dejana.orderservice.model.Order;
import stevanovic.dejana.orderservice.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody CreateOrderRequest createOrderRequest) {
        orderService.createOrder(createOrderRequest);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable Long id, @RequestBody UpdateOrderRequest updateOrderRequest) {
        orderService.updateOrder(id, updateOrderRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getOrdersForUser(@RequestBody GetOrdersRequest getOrdersRequest) {
        return orderService.getOrdersForUser(getOrdersRequest);
    }

}