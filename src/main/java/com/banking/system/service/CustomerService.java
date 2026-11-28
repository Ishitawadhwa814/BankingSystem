package com.banking.system.service;

import com.banking.system.entity.Customer;
import com.banking.system.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository){
        this.customerRepository=customerRepository;
    }

    public Customer createCustomer(String name, String contact){

        if(name==null || name.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Customer name cannot be empty");
        }

        if(contact == null || !contact.matches("\\d{10}")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Contact must contain exactly 10 digits");
        }

        Customer customer = new Customer(name,contact);
        return customerRepository.save(customer);
    }
}
