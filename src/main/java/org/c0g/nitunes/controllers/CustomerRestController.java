package org.c0g.nitunes.controllers;

import org.c0g.nitunes.dao.exceptions.DataRepositoryError;
import org.c0g.nitunes.controllers.exceptions.ResourceNotFoundException;
import org.c0g.nitunes.dao.ICustomerRepository;
import org.c0g.nitunes.models.Customer;
import org.c0g.nitunes.models.CustomerSpending;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/*
    Handle rest requests to /api/v1/customers
    Delegates data access to dao.CustomerRepository
*/

@RestController
public class CustomerRestController {

    Logger logger = LoggerFactory.getLogger(CustomerRestController.class);

    @Autowired
    ICustomerRepository customerRepository;

    /*
    * GET
    */

    //Get all Customers
    //Not checking anything since an empty list is a valid result
    @GetMapping( ApiConstants.API_BASE_PATH + ApiConstants.CUSTOMERS_PATH )
    public List<Customer> getCustomers() {
        return customerRepository.getAllCustomers();
    }

    //Get Customer by id
    //Throw exception on null from repo (handled at the bottom of this class)
    //Invalid ids (not convertible to an int) is handled by Spring (and also caught at the bottom)
    @GetMapping( ApiConstants.API_BASE_PATH + ApiConstants.CUSTOMERS_PATH +"/{id}" )
    public Customer getCustomer(@PathVariable int id) throws ResourceNotFoundException {
        Customer customer = customerRepository.getCustomer(id);
        if (customer == null)
            throw new ResourceNotFoundException();
        return customer;
    }

    //Get number of customers in countries
    //not doing any checking here since an empty map is a valid result
    @GetMapping( ApiConstants.API_BASE_PATH + ApiConstants.CUSTOMERS_PATH + "/countries")
    public Map<String, Integer> getCustomersInCountries() {
        return customerRepository.getCustomersInCountries();
    }

    //Get the top 50 spenders
    //once again no checking since an empty list is valid (we might be a very unpopular store...)
    @GetMapping( ApiConstants.API_BASE_PATH + ApiConstants.CUSTOMERS_PATH +"/spenders")
    public List<CustomerSpending> getCustomerSpenders() {
        return customerRepository.getCustomerSpenders();
    }

    //Get a customers favorite genre(s)
    //throw exception if the customer does not exist
    @GetMapping( ApiConstants.API_BASE_PATH + ApiConstants.CUSTOMERS_PATH + "/{id}/genre" )
    public List<String> getCustomerGenre(@PathVariable int id) throws ResourceNotFoundException {
        if (customerRepository.getCustomer(id) == null)
            throw new ResourceNotFoundException();
        return customerRepository.getCustomerGenre(id);
    }

    /*
    * PUT
    */

    //Update (replace) a customer and return the updated customer object
    //FirstName, LastName & Email must not be null according to the DB schema
    @PutMapping( ApiConstants.API_BASE_PATH + ApiConstants.CUSTOMERS_PATH + "/{id}" )
    public Customer putCustomer(@RequestBody Customer customer, @PathVariable int id) {
        if (customer.getEmail() == null || customer.getLastName() == null || customer.getFirstName() == null)
            throw new IllegalArgumentException();
        return customerRepository.updateCustomer(customer, id, true);
    }

    /*
    * PATCH
    */

    //Update customer fields and return the updated customer object
    //Not checking for the "not null" fields here since the repository method does that for us
    //when called with replace = false.
    //Errors in field names in the json request body are silently ignored
    @PatchMapping( ApiConstants.API_BASE_PATH + ApiConstants.CUSTOMERS_PATH + "/{id}" )
    public Customer patchCustomer(@RequestBody Customer customer, @PathVariable int id) {
        return customerRepository.updateCustomer(customer, id, false);
    }

    /*
    * POST
    */

    //Create a customer and return the created customer object
    //FirstName, LastName & Email must not be null according to the DB schema
    @PostMapping( ApiConstants.API_BASE_PATH + ApiConstants.CUSTOMERS_PATH )
    @ResponseStatus( HttpStatus.CREATED )
    public Customer createCustomer(@RequestBody Customer customer) {
        if (customer.getEmail() == null || customer.getLastName() == null || customer.getFirstName() == null)
            throw new IllegalArgumentException();
        return customerRepository.createCustomer(customer);
    }

    /*
    * EXCEPTION HANDLING
    */

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus( HttpStatus.BAD_REQUEST )
    public void IllegalArgumentHandler(HttpServletRequest req, Exception ex) {
        logger.info("Invalid request received: " + req.getRequestURI());
        logger.info("Invalid request received: " + req.getMethod());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus( HttpStatus.NOT_FOUND )
    public void ResourceNotFound(HttpServletRequest req, Exception ex) {
        logger.info("Resource not found: " + req.getRequestURI());
        logger.info("Request received: " + req.getMethod());
    }

    @ExceptionHandler(DataRepositoryError.class)
    @ResponseStatus( HttpStatus.INTERNAL_SERVER_ERROR )
    public void DataRepositoryError(HttpServletRequest req, Exception ex) {
        logger.info("Repo Error: " + req.getRequestURI());
        logger.info("Request Received: " + req.getMethod());
    }
}
