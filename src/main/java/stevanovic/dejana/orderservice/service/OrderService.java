package stevanovic.dejana.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import stevanovic.dejana.orderservice.dto.CreateOrderRequest;
import stevanovic.dejana.orderservice.dto.GetOrdersRequest;
import stevanovic.dejana.orderservice.dto.ProductResponse;
import stevanovic.dejana.orderservice.dto.UpdateOrderRequest;
import stevanovic.dejana.orderservice.model.Order;
import stevanovic.dejana.orderservice.repository.OrderRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static stevanovic.dejana.orderservice.util.ErrorCodes.CREATE_ORDER_PRODUCT_NOT_IN_STOCK;
import static stevanovic.dejana.orderservice.util.ErrorCodes.UPDATE_ORDER_DIFFERENT_IDS_IN_REQUEST;
import static stevanovic.dejana.orderservice.util.ErrorCodes.UPDATE_ORDER_NOT_EXISTING_IN_REQUEST;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public void createOrder(CreateOrderRequest createOrderRequest) {
        if (isProductInStock(createOrderRequest.getProductIds())){
            Order order = Order.builder()
                    .userId(createOrderRequest.getUserId())
                    .productIds(createOrderRequest.getProductIds())
                    .build();

            // publish Order Created Event
            orderRepository.save(order);
            log.info("Order {} is saved", order.getId());
        } else {
            throw new IllegalArgumentException(CREATE_ORDER_PRODUCT_NOT_IN_STOCK.name());
        }
    }

    private boolean isProductInStock(String productIds) {
        ProductResponse[] productResponseArray = webClientBuilder.build().get()
                .uri("http://product-service/api/product",
                        uriBuilder -> uriBuilder.queryParam("productIds", productIds).build())
                .retrieve()
                .bodyToMono(ProductResponse[].class)
                .block();

        return Arrays.stream(productResponseArray)
                .allMatch(ProductResponse::isInStock);
    }

    public void updateOrder(Long id, UpdateOrderRequest updateOrderRequest) {
        if (!id.equals(updateOrderRequest.getId())) {
            throw new IllegalArgumentException(UPDATE_ORDER_DIFFERENT_IDS_IN_REQUEST.name());
        }

        Optional<Order> existingOrderOptional = orderRepository.findById(id);
        if (existingOrderOptional.isEmpty()) {
            throw new IllegalArgumentException(UPDATE_ORDER_NOT_EXISTING_IN_REQUEST.name());
        }

        Order updatedOrder = existingOrderOptional.get().toBuilder()
                .userId(updateOrderRequest.getUserId())
                .productIds(updateOrderRequest.getProductIds())
                .build();

        orderRepository.save(updatedOrder);
        log.info("Order {} is updated", updatedOrder.getId());
    }

    public void deleteOrder(Long id) {
        Optional<Order> existingOrderOptional = orderRepository.findById(id);
        if (existingOrderOptional.isEmpty()) {
            throw new IllegalArgumentException(UPDATE_ORDER_NOT_EXISTING_IN_REQUEST.name());
        }

        orderRepository.deleteById(id);
        log.info("Order {} is deleted", id);
    }

    public List<Order> getOrdersForUser(GetOrdersRequest getOrdersRequest) {
        return orderRepository.findAllByUserId(getOrdersRequest.getUserId());
    }
}
