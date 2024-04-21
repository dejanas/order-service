package stevanovic.dejana.orderservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import stevanovic.dejana.orderservice.dto.CreateOrderRequest;
import stevanovic.dejana.orderservice.dto.GetOrdersRequest;
import stevanovic.dejana.orderservice.dto.UpdateOrderRequest;
import stevanovic.dejana.orderservice.model.Order;
import stevanovic.dejana.orderservice.service.AuthenticationService;
import stevanovic.dejana.orderservice.service.OrderService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<?> create(HttpServletRequest request,
                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String jwtToken,
                                    @RequestBody CreateOrderRequest createOrderRequest) throws URISyntaxException {
        if (authenticationService.validateToken(jwtToken)) {
            Order order = orderService.createOrder(jwtToken, createOrderRequest);
            String uri = request.getRequestURI() + "/" + order.getId();

            return ResponseEntity
                    .created(new URI(uri))
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@RequestHeader(HttpHeaders.AUTHORIZATION) String jwtToken,
                                    @PathVariable Long id, @RequestBody UpdateOrderRequest updateOrderRequest) {
        if (authenticationService.validateOwnerToken(jwtToken, updateOrderRequest.getUserId())) {
            orderService.updateOrder(id, updateOrderRequest);

            return ResponseEntity
                    .noContent()
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(HttpHeaders.AUTHORIZATION) String jwtToken,
                                    @PathVariable Long id) {
        if (authenticationService.validateAdminToken(jwtToken)) {
            orderService.deleteOrder(id);

            return ResponseEntity
                    .noContent()
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping
    public ResponseEntity<?> getOrdersForUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String jwtToken,
                                              @RequestBody GetOrdersRequest getOrdersRequest) {
        if (authenticationService.validateOwnerToken(jwtToken, getOrdersRequest.getUserId())) {
            return ResponseEntity.ok(orderService.getOrdersForUser(getOrdersRequest));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}