package com.example.pi.dto.request;

import com.example.pi.entity.Customers;

public class UserRegisterRequest {
    private String userName;
    private String password;
    private String roles;
    private Customers customerDetails;

    public UserRegisterRequest() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Customers getCustomerDetails() {
        return customerDetails;
    }

    public void setCustomerDetails(Customers customerDetails) {
        this.customerDetails = customerDetails;
    }
}
