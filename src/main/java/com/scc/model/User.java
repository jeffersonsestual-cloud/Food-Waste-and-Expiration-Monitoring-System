package com.scc.model;

public record User (int user_id,String name, String username, String password, String role, String status) {
    @Override
            public String toString(){
        return String.format("[%5] %s (%s) - Status: %s", user_id, name, username, password, role, status);
    }
}
