package com.learning.bookstore_webapp.clients.orders;

public record OrderConfirmationDTO(String orderNumber, OrderStatus status) {}
