package com.programmingtechie.anotherservice.service;
import com.programmingtechie.anotherservice.dto.InventoryResponse;
import com.programmingtechie.anotherservice.dto.OrderRequest;
import com.programmingtechie.anotherservice.dto.OrderlineItemsDto;
import com.programmingtechie.anotherservice.model.Order;
import com.programmingtechie.anotherservice.model.OrderLineItems;
import com.programmingtechie.anotherservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient webClient;
    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderlineItemsDtoList()
                .stream()
                .map(this::mapToDo)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

      List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode)
                .toList();
        //Call Inventory service and place order if the product is in stock
     InventoryResponse[] inventoryResponsesArray = webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                        .retrieve()
                                .bodyToMono(InventoryResponse[].class)
                                        .block();
       boolean allProductsInStock = Arrays.stream(inventoryResponsesArray)
               .allMatch(InventoryResponse::isInStock);
        if(allProductsInStock) {
            orderRepository.save(order);
        }else {
            throw new IllegalArgumentException("Product is not in stock, please try again later");
        }
    }

    private OrderLineItems mapToDo(OrderlineItemsDto orderlineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderlineItemsDto.getPrice());
        orderLineItems.setQuantity(orderlineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderlineItemsDto.getSkuCode());
        return orderLineItems;
    }

}
