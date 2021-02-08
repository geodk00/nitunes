# Task 4 - Not Itunes
This is an implementation of the "store" described in task 4.

The requirements were:
 - Two views created in Thymeleaf
 - A number of API endpoints

## Deployment
This project as been deployed as a docker container to Heroku.

Commands used to deploy:

```
heroku login
heroku create nitunes --region eu
heroku container:login
heroku container:push web --app nitunes
heroku container:release -a nitunes web
```
## API

Get all customers: `GET /api/v1/customers`

Get customer: `GET /api/v1/customers/{id}`

Create customer: `POST /api/v1/customers`

Update customer: `PUT|PATCH /api/v1/customers/{id}`

Get Customers in countries: `GET /api/v1/customers/countries`

Get top 50 spenders: `GET /api/v1/customers/spenders`

Get customer's favorite genre: `GET /api/v1/customers/{id}/genre`

## Project Structure
The project is split into three sub packages:

### `org.c0g.nitunes.controllers`
This packages contains the two controllers, WebController for Thymeleaf and CustomerRestController for rest.

These controllers use classes in the `dao` package to access the db.

### `org.c0g.nitunes.dao`
This packages contains the repositories for the db types.

### `org.c0g.nitunes.model`
This pacakges contains the data model used from the db.

## Postman
The postman directory contains a collection of postman requests to test the API