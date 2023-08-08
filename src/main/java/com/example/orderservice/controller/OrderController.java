package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order-service")
public class OrderController {
    private final Environment environment;
    private final OrderService orderService;
    private final KafkaProducer kafkaProducer;
    private final OrderProducer orderProducer;

    @GetMapping("/health_check")
    public String status (){
        return String.format ( "It's working in User Service on Port %s",
                                environment.getProperty ( "local.server.port" ) );
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity <ResponseOrder> createdOrder (@PathVariable("userId") String userId,
                                                    @RequestBody RequestOrder requestOrder ){
        ModelMapper modelMapper = new ModelMapper ( );
        modelMapper.getConfiguration ( ).setMatchingStrategy ( MatchingStrategies.STRICT );

        OrderDto dto = modelMapper.map ( requestOrder, OrderDto.class );
        dto.setUserId ( userId );
        /* JPA */
//        OrderDto createdOrder = orderService.createOrder ( dto );
//        ResponseOrder responseUser = modelMapper.map ( createdOrder, ResponseOrder.class );
        /* kafka */
        dto.setOrderId ( UUID.randomUUID ( ).toString ( ) );
        dto.setTotalPrice ( requestOrder.getUnitPrice () * requestOrder.getQty () );
        /* send this order */
        kafkaProducer.send ( "example-catalog-topic", dto );
        orderProducer.send ( "orders", dto );

        ResponseOrder responseUser = modelMapper.map ( dto, ResponseOrder.class );
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( responseUser );
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List <ResponseOrder>> getOrders (@PathVariable("userId") String userId ){
        Iterable<OrderEntity> userList = orderService.getOrderByUserId ( userId );
        List<ResponseOrder> result = new ArrayList <> ();
        userList.forEach ( item -> result.add ( new ModelMapper ().map ( item, ResponseOrder.class ) ) );
        return ResponseEntity.status ( HttpStatus.OK ).body ( result );
    }
}
