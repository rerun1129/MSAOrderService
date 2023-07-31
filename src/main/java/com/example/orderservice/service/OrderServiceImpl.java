package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.jpa.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public OrderDto createOrder ( OrderDto orderDto ) {
        orderDto.setOrderId ( UUID.randomUUID ( ).toString ( ) );
        orderDto.setTotalPrice ( orderDto.getUnitPrice () * orderDto.getQty () );
        ModelMapper modelMapper = new ModelMapper ( );
        modelMapper.getConfiguration ( ).setMatchingStrategy ( MatchingStrategies.STRICT );
        OrderEntity entity = modelMapper.map ( orderDto, OrderEntity.class );
        orderRepository.save ( entity );

        return modelMapper.map ( entity, OrderDto.class );
    }

    @Override
    public OrderDto getOrderByOrderId ( String orderId ) {
        return new ModelMapper ().map ( orderRepository.findByOrderId ( orderId ), OrderDto.class );
    }

    @Override
    public Iterable <OrderEntity> getOrderByUserId ( String userId ) {
        return orderRepository.findByUserId ( userId );
    }
}
