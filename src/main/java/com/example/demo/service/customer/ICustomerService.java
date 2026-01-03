package com.example.demo.service.customer;

import com.example.demo.model.Customer;

import java.util.List;

public interface ICustomerService {
    Customer createCustomer(Customer customer);

    Customer getCustomerById(Long id);


    Customer getCustomerByEmail(String email);

    List<Customer> getAllCustomers();

    Customer updateCustomer(Long id, Customer updatedCustomer);

    void deleteCustomer(Long id);
}
