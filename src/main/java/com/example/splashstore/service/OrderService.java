package com.example.splashstore.service;

import com.example.splashstore.dto.OrderItemRequest;
import com.example.splashstore.dto.OrderItemResponse;
import com.example.splashstore.dto.OrderRequest;
import com.example.splashstore.dto.OrderResponse;
import com.example.splashstore.dto.AddressResponse;
import com.example.splashstore.dto.UserResponse;
import com.example.splashstore.model.AddressModel;
import com.example.splashstore.model.AppUser;
import com.example.splashstore.model.OrderItemModel;
import com.example.splashstore.model.OrderModel;
import com.example.splashstore.model.ProductModel;
import com.example.splashstore.repository.AddressRepository;
import com.example.splashstore.repository.OrderRepository;
import com.example.splashstore.repository.ProductRepository;
import com.example.splashstore.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AppUserRepository userRepository;
    private final AddressRepository addressRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, AppUserRepository userRepository, AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    private AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        AppUser currentUser = getCurrentUser();
        AddressModel address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        boolean isOwner = address.getUser() != null && address.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().equalsIgnoreCase("admin");
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to use this address");
        }

        OrderModel order = new OrderModel();
        order.setAddress(address);
        order.setCreatedBy(currentUser);
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductModel product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + itemRequest.getProductId()));

            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(subtotal);

            OrderItemModel orderItem = new OrderItemModel();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);
            order.addItem(orderItem);
        }

        order.setTotalAmount(total);
        OrderModel savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        AppUser currentUser = getCurrentUser();
        return orderRepository.findByCreatedById(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        OrderModel order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        AppUser currentUser = getCurrentUser();
        boolean isOwner = order.getCreatedBy() != null && order.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().equalsIgnoreCase("admin");

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this order");
        }

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        AppUser currentUser = getCurrentUser();
        if (currentUser.getRole() == null || !currentUser.getRole().equalsIgnoreCase("admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(OrderModel order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setAddress(mapAddressToResponse(order.getAddress()));
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());

        if (order.getCreatedBy() != null) {
            AppUser user = order.getCreatedBy();
            response.setCreatedBy(new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getRole(),
                    user.getFullname(),
                    user.getPhone()
            ));
        }

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::mapItemToResponse)
                .toList();
        response.setItems(itemResponses);
        return response;
    }

    private AddressResponse mapAddressToResponse(AddressModel address) {
        if (address == null) {
            return null;
        }

        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setLabel(address.getLabel());
        response.setStreet(address.getStreet());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPostalCode(address.getPostalCode());
        response.setCountry(address.getCountry());
        response.setPhone(address.getPhone());
        response.setIsDefault(address.getIsDefault());
        response.setCreatedAt(address.getCreatedAt());
        return response;
    }

    private OrderItemResponse mapItemToResponse(OrderItemModel item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setSubtotal(item.getSubtotal());
        return response;
    }
}

