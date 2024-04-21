package stevanovic.dejana.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import stevanovic.dejana.orderservice.dto.CreateOrderRequest;
import stevanovic.dejana.orderservice.dto.GetOrdersRequest;
import stevanovic.dejana.orderservice.dto.ProductResponse;
import stevanovic.dejana.orderservice.dto.UpdateOrderRequest;
import stevanovic.dejana.orderservice.model.Order;
import stevanovic.dejana.orderservice.repository.OrderRepository;

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
    private final RestTemplate restTemplate;
    private static final String PRODUCT_SERVICE_URL = "http://product-service/api/product";

    public Order createOrder(String jwtToken, CreateOrderRequest createOrderRequest) {
        if (isProductInStock(jwtToken, createOrderRequest.getProductIds())) {
            Order order = Order.builder()
                    .userId(createOrderRequest.getUserId())
                    .productIds(createOrderRequest.getProductIds())
                    .build();

            return orderRepository.save(order);
        } else {
            throw new IllegalArgumentException(CREATE_ORDER_PRODUCT_NOT_IN_STOCK.name());
        }
    }

// Reactive version
//    private boolean isProductInStock(String productIds) {
//        ProductResponse[] productResponseArray = webClientBuilder.build().get()
//                .uri(PRODUCT_SERVICE_URL,
//                        uriBuilder -> uriBuilder.queryParam("productIds", productIds).build())
//                .retrieve()
//                .bodyToMono(ProductResponse[].class)
//                .block();
//
//        return Arrays.stream(productResponseArray)
//                .allMatch(ProductResponse::isInStock);
//    }

    public boolean isProductInStock(String jwtToken, String productIds) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        List<String> productIdList = List.of(productIds.split(","));
        boolean isInStock = true;

        for (String productId : productIdList) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(PRODUCT_SERVICE_URL)
                    .queryParam("id", productId);

            ResponseEntity<ProductResponse> response =
                    restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, ProductResponse.class);

            isInStock &= response.getBody() != null;
        }

        return isInStock;
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
