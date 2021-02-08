package org.c0g.nitunes.dao;

import org.c0g.nitunes.models.Customer;
import org.c0g.nitunes.models.CustomerSpending;

import java.util.List;
import java.util.Map;

public interface ICustomerRepository {
    List<Customer> getAllCustomers();
    Customer createCustomer(Customer customer);
    public Customer getCustomer(int id);
    Customer updateCustomer(Customer customer, int id, boolean replace);
    Map<String, Integer> getCustomersInCountries();
    List<CustomerSpending> getCustomerSpenders();
    List<String> getCustomerGenre(int id);
}
