package com.scc.model;

public record FoodItem(int item_id, String name, int quantity, String expiry_date, String status) {
}