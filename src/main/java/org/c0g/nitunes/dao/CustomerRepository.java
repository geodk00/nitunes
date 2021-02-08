package org.c0g.nitunes.dao;

import org.c0g.nitunes.dao.exceptions.DataRepositoryError;
import org.c0g.nitunes.models.Customer;
import org.c0g.nitunes.models.CustomerSpending;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    Provides access to everything to do with customers in the database
    Number of rows returned from the db and stuff like that
    is generally not checked and empty lists/nulls
    are returned instead and handled by the client classes.

    All methods throw DataRepositoryError on SQL errors (hiding the implementation detail of what kind of DB we're using?).
    This exception is handled in the rest controller
    and signalled to the client with a status code of 500

    I have not commented every method here since they mostly map 1:1 to the rest controller
 */

@Component
public class CustomerRepository implements ICustomerRepository {
    Logger logger = LoggerFactory.getLogger(CustomerRepository.class);

    @Override
    public List<Customer> getAllCustomers() {
        final String sql = "SELECT CustomerId, FirstName, LastName, Country, PostalCode, Phone, Email FROM Customer";
        List<Customer> customerList = new ArrayList<>();

        //using try-with-resources instead of closing manually.
        try ( Connection connection = DriverManager.getConnection(ConnectionHelper.URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            logger.info("Connected to DB");

            //using try-with-resources instead of closing manually.
            try (ResultSet result = statement.executeQuery()) {
                logger.info("Executed getAllCustomers-query");
                while (result.next()) {
                    customerList.add(createCustomerFromResult(result));
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getLocalizedMessage());
            throw new DataRepositoryError();
        }
        return customerList;
    }

    @Override
    public Customer getCustomer(int id) {
        final String sql = "SELECT CustomerId, FirstName, LastName, Country, PostalCode, Phone, Email FROM `Customer` " +
                            "WHERE CustomerId = ?";
        Customer customer = null;

        //using try-with-resources instead of closing manually.
        try ( Connection connection = DriverManager.getConnection(ConnectionHelper.URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            logger.info("Connected to DB");

            //using try-with-resources instead of closing manually.
            try (ResultSet result = statement.executeQuery()) {
                logger.info("Executed getCustomer-query");
                while (result.next()) {
                    customer = createCustomerFromResult(result);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getLocalizedMessage());
            throw new DataRepositoryError();
        }
        return customer;
    }

    @Override
    public Customer createCustomer(Customer customer) {
        final String sql = "INSERT INTO Customer (FirstName, LastName, Country, PostalCode, Phone, Email) VALUES" +
                            "(?, ?, ?, ?, ?, ?)";
        try ( Connection connection = DriverManager.getConnection(ConnectionHelper.URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getLastName());
            statement.setString(3, customer.getCountry());
            statement.setString(4, customer.getPostalCode());
            statement.setString(5, customer.getPhone());
            statement.setString(6, customer.getEmail());

            logger.info("Connected to DB");
            statement.executeUpdate();
            logger.info("Executed createCustomer-query");

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                //move cursor to the first result and retrieve the id of the created customer
                resultSet.next();
                int customerId = resultSet.getInt(1);
                logger.info("Inserted Customer with ID: " + customerId);
                return getCustomer(customerId);
            }
        } catch (SQLException ex) {
            logger.error(ex.getLocalizedMessage());
            throw new DataRepositoryError();
        }
    }

    /*
    Update a customer. If replace is true, all fields are updated
    otherwise only fields that are not null.

    I'm fairly certain that the way I'm constructing the
    sql string does not open me up to injection attacks since
    I still add the values from the client via the prepared statement
    */
    @Override
    public Customer updateCustomer(Customer customer, int id, boolean replace) {
        final String sql = "UPDATE Customer SET %s WHERE CustomerId = ?";
        //Map to hold the fields to update and their values
        Map<String, String> fields = new HashMap<>();

        //Add all fields regardless of value if replace == true, otherwise only non-null fields
        if (replace || customer.getFirstName() != null)
            fields.put("FirstName", customer.getFirstName());
        if (replace || customer.getLastName() != null)
            fields.put("LastName", customer.getLastName());
        if(replace || customer.getCountry() != null)
            fields.put("Country", customer.getCountry());
        if (replace || customer.getPostalCode() != null)
            fields.put("PostalCode", customer.getPostalCode());
        if (replace || customer.getPhone() != null)
            fields.put("Phone", customer.getPhone());
        if (replace || customer.getEmail() != null)
            fields.put("Email", customer.getEmail());

        //only perform update if there's actual work to do
        if (fields.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();

            //Construct string with [field] = ? pairs
            for (var entry : fields.keySet()) {
                if (!stringBuilder.isEmpty())
                    stringBuilder.append(",");
                stringBuilder.append(String.format(" %s = ?", entry));
            }

            //Add the constructed string to the sql string
            String finalSql = String.format(sql, stringBuilder.toString());

            try ( Connection connection = DriverManager.getConnection(ConnectionHelper.URL);
                 PreparedStatement statement = connection.prepareStatement(finalSql)) {

                //add the data from the client to the prepared statement
                int sqlParam = 1;
                for (var value : fields.values()) {
                    statement.setString(sqlParam, value);
                    sqlParam++;
                }
                statement.setInt(sqlParam, id);

                logger.info("Connected to DB");
                statement.executeUpdate();
                logger.info("Executed updateCustomer-query");
            } catch (SQLException ex) {
                logger.error(ex.getLocalizedMessage());
                throw new DataRepositoryError();
            }
        } else {
            logger.info("Nothing to do in updateCustomer");
        }
        return getCustomer(id);
    }

    @Override
    public Map<String, Integer> getCustomersInCountries() {
        final String sql = "SELECT COUNT(CustomerId), Country FROM Customer WHERE Country NOT NULL GROUP BY Country ORDER BY COUNT(CustomerId) DESC";

        //Map to hold Country -> Number of customers in that country
        Map<String, Integer> resultMap = new HashMap<>();

        //using try-with-resources instead of closing manually.
        try ( Connection connection = DriverManager.getConnection(ConnectionHelper.URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            logger.info("Connected to DB");

            try (ResultSet result = statement.executeQuery()) {
                logger.info("Executed getAllCustomers-query");
                while (result.next()) {
                    resultMap.put(result.getString(2), result.getInt(1));
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getLocalizedMessage());
            throw new DataRepositoryError();
        }
        return resultMap;
    }

    @Override
    public List<CustomerSpending> getCustomerSpenders() {
        final String sql = """
                SELECT 
                SUM(Invoice.Total), Customer.CustomerId, Customer.FirstName, 
                Customer.LastName, Customer.Email 
                FROM Invoice JOIN Customer ON Customer.CustomerId = Invoice.CustomerId
                GROUP BY Invoice.CustomerId 
                ORDER BY Sum(Total) DESC LIMIT 50""";

        List<CustomerSpending> resultList = new ArrayList<>();

        //using try-with-resources instead of closing manually.
        try( Connection connection = DriverManager.getConnection(ConnectionHelper.URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            logger.info("Connected to DB");

            try (ResultSet result = statement.executeQuery()) {
                logger.info("Executed getCustomerSpenders-query");
                while (result.next()) {
                    CustomerSpending customer = new CustomerSpending(result.getInt("CustomerId"),
                                                        result.getString("FirstName"),
                                                        result.getString("LastName"),
                                                        result.getString("Email"),
                                                        result.getDouble(1));
                    resultList.add(customer);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getLocalizedMessage());
            throw new DataRepositoryError();
        }
        return resultList;
    }

    /*
        Get a customers favorite genre(s).
        I resolve genre ties in the client instead of doing it in
        the sql statement. Not much difference since we're using sqlite and
        we're doing all the DB work locally anyway.
    */

    @Override
    public List<String> getCustomerGenre(int id) {
        final String sql = """
                SELECT Genre.Name, Count(Genre.Name) 
                FROM Invoice 
                JOIN InvoiceLine ON Invoice.InvoiceId = InvoiceLine.InvoiceId 
                JOIN Track ON InvoiceLine.TrackId = Track.TrackId 
                JOIN Genre ON Track.GenreId = Genre.GenreId 
                WHERE Invoice.CustomerId = ? 
                GROUP BY Genre.Name 
                ORDER BY Count(Genre.Name) DESC            
                """;

        List<String> resultList = new ArrayList<>();
        int genreCount = 0;

        //using try-with-resources instead of closing manually.
        try ( Connection connection = DriverManager.getConnection(ConnectionHelper.URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            logger.info("Connected to DB");
            statement.setInt(1, id);

            //using try-with-resources instead of closing manually.
            try (ResultSet result = statement.executeQuery()) {
                logger.info("Executed getCustomerGenre-query");
                while (result.next()) {
                    //Set genreCount to the value of the first row
                    if (genreCount == 0) {
                        genreCount = result.getInt(2);
                        resultList.add(result.getString(1));
                    } else if (genreCount == result.getInt(2)) { //add genres with the same count as the first to the list
                        resultList.add(result.getString(1));
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getLocalizedMessage());
        }
        return resultList;
    }

    //Helper method for turning a ResultSet into a Customer
    private Customer createCustomerFromResult(ResultSet result) {
        Customer customer = new Customer();
        try {
            customer.setCustomerId(result.getInt("CustomerId"));
            customer.setFirstName(result.getString("FirstName"));
            customer.setLastName(result.getString("LastName"));
            customer.setCountry(result.getString("Country"));
            customer.setPostalCode(result.getString("PostalCode"));
            customer.setPhone(result.getString("Phone"));
            customer.setEmail(result.getString("Email"));
        } catch (SQLException ex) {
            logger.error(ex.getLocalizedMessage());
        }
        return customer;
    }
}
