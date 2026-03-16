# E-Commerce Backend System (Spring Boot)

## Project overview

A production-style backend for an e-commerce platform built with **Java 17** and **Spring Boot 3**.
Supports user registration/login, role-based access (ADMIN / CUSTOMER), product catalog with pagination/filtering/search, shopping cart, checkout → order creation, simulated payments, and inventory management. The app follows a layered Controller → Service → Repository → Entity architecture, uses JWT based security, includes unit tests, and a test coverage report.

**Tech stack (high level)**

| Component          | Technology                  |
| ------------------ | --------------------------- |
| Language           | Java 17                     |
| Framework          | Spring Boot 3               |
| ORM                | Spring Data JPA / Hibernate |
| Database           | MySQL                       |
| Security           | Spring Security + JWT       |
| Build tool         | Maven                       |
| Testing            | JUnit 5, Mockito            |
| API docs           | Swagger (OpenAPI)           |
| Containerization   | Docker                      |
| Repo / hosting     | GitHub                      |


---

## Quick structure & conventions

Project package structure (high level):

```
com.ecommerce
 ┣ controller
 ┣ service
 ┣ repository
 ┣ entity
 ┣ dto
 ┣ exception
 ┣ config
 ┗ utils
```

Layering: `Controller → Service → Repository → Entity`

---

## How to run locally

1. **Clone the repo**

```bash
git clone https://github.com/CHITTARANJANMOHANTY-2003/ecommerce-backend.git
cd ecommerce-backend
```

2. **Create DB**

```sql
CREATE DATABASE ecommerce_db;
```

3. **Configure properties**

Create `src/main/resources/application.properties` **locally** (do not commit secrets), or add `application.properties.example` (committed) with placeholders:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=your_db_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

app.jwt.secret=replace_with_strong_secret
app.jwt.expiration=3600000
```

4. **Build**

```bash
mvn clean install
```

5. **Run**

```bash
mvn spring-boot:run
# or
java -jar target/ecommerce-backend.jar
```

6. **Swagger UI**
   Open:

```
http://localhost:8080/swagger-ui/index.html
```

---

## Security — JWT (how it works & how to use)

**Implementation notes (from your `SecurityConfig`):**

* Password hashing: `BCryptPasswordEncoder`.
* Stateless authentication: `SessionCreationPolicy.STATELESS`.
* Authentication implemented with `DaoAuthenticationProvider` + `CustomUserDetailsService`.
* JWT is validated via `JwtAuthenticationFilter` added before `UsernamePasswordAuthenticationFilter`.
* Public endpoints (no auth): Swagger endpoints, `POST /api/users/register`, `POST /api/users/login`, `GET /api/products/**`, `GET /api/products/search`.
* Role enforcement uses `hasRole("ADMIN")` and `hasAnyRole("CUSTOMER","ADMIN")`. Your stored enum values use the `ROLE_` prefix (e.g., `ROLE_ADMIN`).
* A default admin is created at startup (`admin@gmail.com` / `admin123`) if not present (change for production).

**How to obtain & use the JWT token (typical flow)**

1. `POST /api/users/register` — create a user (optional if you will use the default admin).
2. `POST /api/users/login` — returns a JWT token in the response (e.g., field `token` or `accessToken`).
3. For protected endpoints add HTTP header:

```
Authorization: Bearer <JWT_TOKEN>
```

4. Token expiration is controlled via `app.jwt.expiration` — re-login when token expires.

---

## Full API Documentation (Each Endpoint, Auth & Example)

> Replace `{{baseUrl}}` with `http://localhost:8080`.  
> For protected endpoints include header `Authorization: Bearer <JWT>`.

---

## AUTH / USER APIs

### POST /api/users/register

**Access:** Public  
**Description:** Register a new user.

#### Request

**Headers**

- `Content-Type: application/json`

**Request Body**

```json
{
  "name": "Rahul Sharma",
  "email": "rahul.sharma@gmail.com",
  "password": "Rahul@123"
}
```
```bash
curl -X POST "{{baseUrl}}/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rahul Sharma",
    "email": "rahul.sharma@gmail.com",
    "password": "Rahul@123"
  }'
 ``` 
Response (200)
```
{
  "id": 2,
  "name": "Rahul Sharma",
  "email": "rahul.sharma@gmail.com",
  "password": null,
  "role": "ROLE_CUSTOMER"
}
```
| Code | Description                                                |
| ---- | ---------------------------------------------------------- |
| 200  | Success (example shown above from Postman response)        |
| 201  | Resource created (recommended for successful registration) |
| 400  | Bad request / validation error                             |
| 409  | Conflict (e.g., email already exists)                      |
| 500  | Internal server error                                      |
---
### POST /api/users/login

**Access:** Public  
**Description:** Authenticate a user and return a JWT token for accessing protected endpoints.

#### Request

**Headers**

- `Content-Type: application/json`

**Request Body**

```json
{
  "email": "rahul.sharma@gmail.com",
  "password": "Rahul@123"
}
```
cURL Example
```bash
curl -X POST "{{baseUrl}}/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "rahul.sharma@gmail.com",
    "password": "Rahul@123"
  }'
 ``` 
Response (200)
```
{
  "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJyYWh1bC5zaGFybWFAZ21haWwuY29tIiwiaWF0IjoxNzczNjMyODUyLCJleHAiOjE3NzM3MTkyNTJ9.zvY_8od-p2IkHpwtyaNhE6DnJvMFQq_o-dy9FKcCxl2k43yIzOV_owXSpI7GwWme",
  "email": "rahul.sharma@gmail.com",
  "role": "Login Successful"
}
```
Note: Use the returned token in the Authorization header for all protected endpoints.

Example:

Authorization: Bearer <JWT_TOKEN>
Status Codes
| Code | Description                           |
| ---- | ------------------------------------- |
| 200  | Login successful (JWT token returned) |
| 400  | Bad request / validation error        |
| 401  | Unauthorized — invalid credentials    |
| 500  | Internal server error                 |

---

### Get My Profile

Retrieves the profile details of the currently authenticated user.

#### Endpoint
GET /api/users/me

#### Authorization
Bearer Token (JWT)

#### cURL Example
```bash
curl -X GET "http://localhost:8080/api/users/me" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```
Response (200)
```
{
  "id": 2,
  "name": "Rahul Sharma",
  "email": "rahul.sharma@gmail.com",
  "password": null,
  "role": "ROLE_CUSTOMER"
}
```
Status code

| Code | Description           |
| ---- | --------------------- |
| 200  | Request successful    |
| 401  | Unauthorized          |
| 403  | Forbidden             |
| 500  | Internal server error |
---

### Update Profile

Updates the profile information of the authenticated user.

#### Endpoint

PUT /api/users/me

#### Authorization

Bearer Token (JWT)

#### Request Body
```{
  "name": "Rahul Kumar Sharma",
  "email": "rahul.kumar@gmail.com"
}
```
cURL Example
```bash
curl -X PUT "http://localhost:8080/api/users/me" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json" \
 -d '{
  "name": "Rahul Kumar Sharma",
  "email": "rahul.kumar@gmail.com"
}'
```
Response (200)
```{
  "id": 2,
  "name": "Rahul Kumar Sharma",
  "email": "rahul.kumar@gmail.com",
  "password": null,
  "role": "ROLE_CUSTOMER"
}
```
Status code
| Code | Description                    |
| ---- | ------------------------------ |
| 200  | Profile updated successfully   |
| 400  | Bad request / validation error |
| 401  | Unauthorized                   |
| 403  | Forbidden                      |
| 500  | Internal server error          |

---

### Change Password

Changes the password of the authenticated user.

#### Endpoint

PUT /api/users/me/password

#### Authorization

Bearer Token (JWT)

| Parameter   | Type   | Description               |
| ----------- | ------ | ------------------------- |
| newPassword | String | New password for the user |


Example Request URL

/api/users/me/password?newPassword=Rahul@456

```bash
cURL Example
curl -X PUT "http://localhost:8080/api/users/me/password?newPassword=Rahul@456" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)
Password updated successfully

#### Status Codes

| Code | Description                   |
| ---- | ----------------------------- |
| 200  | Password updated successfully |
| 400  | Bad request                   |
| 401  | Unauthorized                  |
| 403  | Forbidden                     |
| 500  | Internal server error         |

---

# Admin Management APIs

---

### Create Admin

Creates a new administrator account. When we run application first time a admin user will be created by default with user name(admin@gmail.com) and passwor(admin123). so use them to generate token and by using this token we will be able to create new admin.

#### Endpoint

POST /api/admin/create-admin

#### Authorization

Bearer Token (JWT)

#### Request Body

```json
{
  "name": "Super Admin",
  "email": "superadmin@shopkart.com",
  "password": "SuperAdmin@123"
}
```

```bash
cURL Example
curl -X POST "http://localhost:8080/api/admin/create-admin" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json" \
 -d '{
  "name": "Super Admin",
  "email": "superadmin@shopkart.com",
  "password": "SuperAdmin@123"
}'
```

#### Response (200)

```json
{
  "id": 4,
  "name": "Super Admin",
  "email": "superadmin@shopkart.com",
  "password": null,
  "role": "ROLE_ADMIN"
}
```

#### Status Codes

| Code | Description                    |
| ---- | ------------------------------ |
| 200  | Admin created successfully     |
| 400  | Bad request / validation error |
| 401  | Unauthorized                   |
| 403  | Forbidden                      |
| 500  | Internal server error          |

---

### Get All Users

Retrieves a list of all users in the system.

#### Endpoint

GET /api/admin/users

#### Authorization

Bearer Token (JWT)

```bash
cURL Example
curl -X GET "http://localhost:8080/api/admin/users" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
[
  {
    "id": 1,
    "name": "Admin",
    "email": "admin@gmail.com",
    "password": null,
    "role": "ROLE_ADMIN"
  },
  {
    "id": 2,
    "name": "Rahul Kumar Sharma",
    "email": "rahul.kumar@gmail.com",
    "password": null,
    "role": "ROLE_CUSTOMER"
  },
  {
    "id": 3,
    "name": "Priya Das",
    "email": "priya.das@gmail.com",
    "password": null,
    "role": "ROLE_CUSTOMER"
  },
  {
    "id": 4,
    "name": "Super Admin",
    "email": "superadmin@shopkart.com",
    "password": null,
    "role": "ROLE_ADMIN"
  }
]
```

#### Status Codes

| Code | Description                  |
| ---- | ---------------------------- |
| 200  | Users retrieved successfully |
| 401  | Unauthorized                 |
| 403  | Forbidden                    |
| 500  | Internal server error        |

---

### Get User By ID

Retrieves the details of a specific user by their ID.

#### Endpoint

GET /api/admin/users/{id}

#### Authorization

Bearer Token (JWT)

| Parameter | Type    | Description    |
| --------- | ------- | -------------- |
| id        | Integer | ID of the user |

Example Request URL

/api/admin/users/3

```bash
cURL Example
curl -X GET "http://localhost:8080/api/admin/users/3" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
{
  "id": 3,
  "name": "Priya Das",
  "email": "priya.das@gmail.com",
  "password": null,
  "role": "ROLE_CUSTOMER"
}
```

#### Status Codes

| Code | Description                 |
| ---- | --------------------------- |
| 200  | User retrieved successfully |
| 401  | Unauthorized                |
| 403  | Forbidden                   |
| 404  | User not found              |
| 500  | Internal server error       |

---

### Update User

Updates the information of an existing user.

#### Endpoint

PUT /api/admin/users/{id}

#### Authorization

Bearer Token (JWT)

| Parameter | Type    | Description    |
| --------- | ------- | -------------- |
| id        | Integer | ID of the user |

Example Request URL

/api/admin/users/3

#### Request Body

```json
{
  "name": "Priya Dash",
  "email": "priya.dash@gmail.com",
  "role": "ROLE_CUSTOMER"
}
```

```bash
cURL Example
curl -X PUT "http://localhost:8080/api/admin/users/3" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json" \
 -d '{
  "name": "Priya Dash",
  "email": "priya.dash@gmail.com",
  "role": "ROLE_CUSTOMER"
}'
```

#### Response (200)

```json
{
  "id": 3,
  "name": "Priya Dash",
  "email": "priya.dash@gmail.com",
  "password": null,
  "role": "ROLE_CUSTOMER"
}
```

#### Status Codes

| Code | Description               |
| ---- | ------------------------- |
| 200  | User updated successfully |
| 400  | Bad request               |
| 401  | Unauthorized              |
| 403  | Forbidden                 |
| 404  | User not found            |
| 500  | Internal server error     |

---

### Delete User

Deletes a user from the system.

#### Endpoint

DELETE /api/admin/users/{id}

#### Authorization

Bearer Token (JWT)

| Parameter | Type    | Description    |
| --------- | ------- | -------------- |
| id        | Integer | ID of the user |

Example Request URL

/api/admin/users/3

```bash
cURL Example
curl -X DELETE "http://localhost:8080/api/admin/users/3" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

User deleted successfully

#### Status Codes

| Code | Description               |
| ---- | ------------------------- |
| 200  | User deleted successfully |
| 401  | Unauthorized              |
| 403  | Forbidden                 |
| 404  | User not found            |
| 500  | Internal server error     |

---

# Product APIs

---

## Get All Products

Returns a paginated list of all available products.

#### Endpoint

GET /api/products

#### Authorization

None

| Parameter | Type    | Description                            |
| --------- | ------- | -------------------------------------- |
| page      | Integer | Page number (optional)                 |
| size      | Integer | Number of products per page (optional) |
| sort      | String  | Sorting criteria (optional)            |

Example Request URL

/api/products

```bash
cURL Example
curl -X GET "http://localhost:8080/api/products" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
{
  "totalPages": 1,
  "totalElements": 2,
  "size": 10,
  "content": [
    {
      "id": 1,
      "name": "iPhone 13",
      "description": "Apple iPhone 13 with A15 Bionic Chip",
      "price": 65000,
      "stock": 20,
      "category": "Electronics",
      "imageUrl": "https://example.com/iphone13.jpg",
      "rating": 4.7
    },
    {
      "id": 2,
      "name": "Nike Running Shoes",
      "description": "Comfortable running shoes",
      "price": 4500,
      "stock": 50,
      "category": "Fashion",
      "imageUrl": "https://example.com/nike.jpg",
      "rating": 4.3
    }
  ]
}
```

#### Status Codes

| Code | Description                     |
| ---- | ------------------------------- |
| 200  | Products retrieved successfully |
| 400  | Bad request                     |
| 500  | Internal server error           |

---

# Get Product By ID

Returns details of a specific product.

#### Endpoint

GET /api/products/{id}

#### Authorization

None

| Parameter | Type | Description              |
| --------- | ---- | ------------------------ |
| id        | Long | Unique ID of the product |

Example Request URL

/api/products/1

```bash
cURL Example
curl -X GET "http://localhost:8080/api/products/1" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
{
  "id": 1,
  "name": "iPhone 13",
  "description": "Apple iPhone 13 with A15 Bionic Chip",
  "price": 65000,
  "stock": 20,
  "category": "Electronics",
  "imageUrl": "https://example.com/iphone13.jpg",
  "rating": 4.7
}
```

#### Status Codes

| Code | Description                    |
| ---- | ------------------------------ |
| 200  | Product retrieved successfully |
| 404  | Product not found              |
| 500  | Internal server error          |

---

# Search Products

Search products using filters such as category and price range.

#### Endpoint

GET /api/products

#### Authorization

None

| Parameter | Type   | Description                 |
| --------- | ------ | --------------------------- |
| category  | String | Filter products by category |
| minPrice  | Double | Minimum product price       |
| maxPrice  | Double | Maximum product price       |

Example Request URL

/api/products?category=Electronics&minPrice=1000&maxPrice=70000

```bash
cURL Example
curl -X GET "http://localhost:8080/api/products?category=Electronics&minPrice=1000&maxPrice=70000" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
{
  "totalPages": 1,
  "totalElements": 1,
  "size": 10,
  "content": [
    {
      "id": 1,
      "name": "iPhone 13",
      "description": "Apple iPhone 13 with A15 Bionic Chip",
      "price": 65000,
      "stock": 20,
      "category": "Electronics",
      "imageUrl": "https://example.com/iphone13.jpg",
      "rating": 4.7
    }
  ]
}
```

#### Status Codes

| Code | Description                     |
| ---- | ------------------------------- |
| 200  | Products retrieved successfully |
| 400  | Bad request                     |
| 500  | Internal server error           |

---

# Create Product (Admin)

Creates a new product. Only accessible by Admin users.

#### Endpoint

POST /api/products

#### Authorization

Bearer Token (JWT)

| Parameter   | Type    | Description          |
| ----------- | ------- | -------------------- |
| name        | String  | Product name         |
| description | String  | Product description  |
| price       | Double  | Product price        |
| stock       | Integer | Available stock      |
| category    | String  | Product category     |
| imageUrl    | String  | Image URL of product |
| rating      | Double  | Product rating       |

```json
Request Body
{
  "name": "Nike Running Shoes",
  "description": "Comfortable running shoes",
  "price": 4500,
  "stock": 50,
  "category": "Fashion",
  "imageUrl": "https://example.com/nike.jpg",
  "rating": 4.3
}
```

```bash
cURL Example
curl -X POST "http://localhost:8080/api/products" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json" \
 -d '{
  "name": "Nike Running Shoes",
  "description": "Comfortable running shoes",
  "price": 4500,
  "stock": 50,
  "category": "Fashion",
  "imageUrl": "https://example.com/nike.jpg",
  "rating": 4.3
}'
```

#### Response (200)

```json
{
  "id": 2,
  "name": "Nike Running Shoes",
  "description": "Comfortable running shoes",
  "price": 4500,
  "stock": 50,
  "category": "Fashion",
  "imageUrl": "https://example.com/nike.jpg",
  "rating": 4.3
}
```

#### Status Codes

| Code | Description                  |
| ---- | ---------------------------- |
| 200  | Product created successfully |
| 400  | Bad request                  |
| 401  | Unauthorized                 |
| 403  | Forbidden                    |
| 500  | Internal server error        |

---

# Update Product (Admin)

Updates an existing product. Only accessible by Admin users.

#### Endpoint

PUT /api/products/{id}

#### Authorization

Bearer Token (JWT)

| Parameter | Type | Description                 |
| --------- | ---- | --------------------------- |
| id        | Long | ID of the product to update |

```json
Request Body
{
  "name": "Nike Running Shoes",
  "description": "Comfortable running shoes",
  "price": 3500,
  "stock": 75,
  "category": "Fashion",
  "imageUrl": "https://example.com/nike.jpg",
  "rating": 4.6
}
```

Example Request URL

/api/products/2

```bash
cURL Example
curl -X PUT "http://localhost:8080/api/products/2" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json" \
 -d '{
  "name": "Nike Running Shoes",
  "description": "Comfortable running shoes",
  "price": 3500,
  "stock": 75,
  "category": "Fashion",
  "imageUrl": "https://example.com/nike.jpg",
  "rating": 4.6
}'
```

#### Response (200)

```json
{
  "id": 2,
  "name": "Nike Running Shoes",
  "description": "Comfortable running shoes",
  "price": 3500,
  "stock": 75,
  "category": "Fashion",
  "imageUrl": "https://example.com/nike.jpg",
  "rating": 4.6
}
```

#### Status Codes

| Code | Description                  |
| ---- | ---------------------------- |
| 200  | Product updated successfully |
| 400  | Bad request                  |
| 401  | Unauthorized                 |
| 403  | Forbidden                    |
| 404  | Product not found            |
| 500  | Internal server error        |

---

# Delete Product (Admin)

Deletes a product by ID. Only accessible by Admin users.

#### Endpoint

DELETE /api/products/{id}

#### Authorization

Bearer Token (JWT)

| Parameter | Type | Description                 |
| --------- | ---- | --------------------------- |
| id        | Long | ID of the product to delete |

Example Request URL

/api/products/2

```bash
cURL Example
curl -X DELETE "http://localhost:8080/api/products/2" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

```
Product deleted successfully
```

#### Status Codes

| Code | Description                  |
| ---- | ---------------------------- |
| 200  | Product deleted successfully |
| 401  | Unauthorized                 |
| 403  | Forbidden                    |
| 404  | Product not found            |
| 500  | Internal server error        |

---

# Cart APIs

---

# Add Product To Cart

Adds a product to the authenticated user's cart.

#### Endpoint

POST /api/cart/add/{productId}

#### Authorization

Bearer Token (JWT)

| Parameter | Type    | Description              |
| --------- | ------- | ------------------------ |
| productId | Long    | ID of the product to add |
| quantity  | Integer | Quantity of the product  |

Example Request URL

/api/cart/add/1?quantity=2

```bash
cURL Example
curl -X POST "http://localhost:8080/api/cart/add/1?quantity=2" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
{
  "id": 2,
  "userId": 2,
  "totalPrice": 130000,
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 13",
      "quantity": 2,
      "price": 65000,
      "subtotal": 130000
    }
  ]
}
```

#### Status Codes

| Code | Description                        |
| ---- | ---------------------------------- |
| 200  | Product added to cart successfully |
| 401  | Unauthorized                       |
| 404  | Product not found                  |
| 500  | Internal server error              |

---

# Get Cart

Returns the authenticated user's cart details.

#### Endpoint

GET /api/cart

#### Authorization

Bearer Token (JWT)

| Parameter | Type | Description            |
| --------- | ---- | ---------------------- |
| None      | -    | No parameters required |

Example Request URL

/api/cart

```bash
cURL Example
curl -X GET "http://localhost:8080/api/cart" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
{
  "id": 2,
  "userId": 2,
  "totalPrice": 152500,
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 13",
      "quantity": 2,
      "price": 65000,
      "subtotal": 130000
    },
    {
      "productId": 3,
      "productName": "Nike Running Shoes",
      "quantity": 5,
      "price": 4500,
      "subtotal": 22500
    }
  ]
}
```

#### Status Codes

| Code | Description                 |
| ---- | --------------------------- |
| 200  | Cart retrieved successfully |
| 401  | Unauthorized                |
| 500  | Internal server error       |

---

# Update Cart Quantity

Updates the quantity of a specific product in the cart.

#### Endpoint

PUT /api/cart/update/{productId}

#### Authorization

Bearer Token (JWT)

| Parameter | Type    | Description       |
| --------- | ------- | ----------------- |
| productId | Long    | ID of the product |
| quantity  | Integer | Updated quantity  |

Example Request URL

/api/cart/update/1?quantity=5

```bash
cURL Example
curl -X PUT "http://localhost:8080/api/cart/update/1?quantity=5" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
{
  "id": 2,
  "userId": 2,
  "totalPrice": 347500,
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 13",
      "quantity": 5,
      "price": 65000,
      "subtotal": 325000
    },
    {
      "productId": 3,
      "productName": "Nike Running Shoes",
      "quantity": 5,
      "price": 4500,
      "subtotal": 22500
    }
  ]
}
```

#### Status Codes

| Code | Description               |
| ---- | ------------------------- |
| 200  | Cart updated successfully |
| 401  | Unauthorized              |
| 404  | Product not found in cart |
| 500  | Internal server error     |

---

# Remove Item From Cart

Removes a specific product from the cart.

#### Endpoint

DELETE /api/cart/remove/{productId}

#### Authorization

Bearer Token (JWT)

| Parameter | Type | Description                 |
| --------- | ---- | --------------------------- |
| productId | Long | ID of the product to remove |

Example Request URL

/api/cart/remove/1

```bash
cURL Example
curl -X DELETE "http://localhost:8080/api/cart/remove/1" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
{
  "id": 1,
  "userId": 1,
  "totalPrice": 207000,
  "items": [
    {
      "productId": 3,
      "productName": "Nike Running Shoes",
      "quantity": 46,
      "price": 4500,
      "subtotal": 207000
    }
  ]
}
```

#### Status Codes

| Code | Description               |
| ---- | ------------------------- |
| 200  | Item removed successfully |
| 401  | Unauthorized              |
| 404  | Item not found in cart    |
| 500  | Internal server error     |

---

# Clear Cart

Removes all items from the authenticated user's cart.

#### Endpoint

DELETE /api/cart/clear

#### Authorization

Bearer Token (JWT)

| Parameter | Type | Description            |
| --------- | ---- | ---------------------- |
| None      | -    | No parameters required |

Example Request URL

/api/cart/clear

```bash
cURL Example
curl -X DELETE "http://localhost:8080/api/cart/clear" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

Cart cleared successfully

#### Status Codes

| Code | Description               |
| ---- | ------------------------- |
| 200  | Cart cleared successfully |
| 401  | Unauthorized              |
| 500  | Internal server error     |

---


# Order APIs

---

# Checkout Order

Creates a new order from the authenticated user's cart.

#### Endpoint

POST /api/orders/checkout

#### Authorization

Bearer Token (JWT)

| Parameter   | Type   | Description                                 |
| ----------- | ------ | ------------------------------------------- |
| paymentMode | String | Payment method for the order (Example: COD) |

Example Request URL

/api/orders/checkout?paymentMode=COD

```bash
cURL Example
curl -X POST "http://localhost:8080/api/orders/checkout?paymentMode=COD" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
{
  "id": 1,
  "userId": 2,
  "totalAmount": 347500,
  "orderDate": "2026-03-16T04:39:15.110857610Z",
  "paymentStatus": "PENDING",
  "orderStatus": "PENDING_PAYMENT",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 13",
      "quantity": 5,
      "price": 65000
    },
    {
      "productId": 3,
      "productName": "Nike Running Shoes",
      "quantity": 5,
      "price": 4500
    }
  ]
}
```

#### Status Codes

| Code | Description                |
| ---- | -------------------------- |
| 200  | Order created successfully |
| 401  | Unauthorized               |
| 500  | Internal server error      |

---

# Pay Order

Processes payment for an order.

#### Endpoint

POST /api/orders/{orderId}/pay

#### Authorization

Bearer Token (JWT)

| Parameter | Type    | Description                                      |
| --------- | ------- | ------------------------------------------------ |
| orderId   | Long    | ID of the order                                  |
| success   | Boolean | Payment result (true = success, false = failure) |

Example Request URL

/api/orders/1/pay?success=true

```bash
cURL Example
curl -X POST "http://localhost:8080/api/orders/1/pay?success=true" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
{
  "id": 1,
  "userId": 2,
  "totalAmount": 347500,
  "orderDate": "2026-03-16T04:39:15.110858Z",
  "paymentStatus": "SUCCESS",
  "orderStatus": "PLACED",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 13",
      "quantity": 5,
      "price": 65000
    },
    {
      "productId": 3,
      "productName": "Nike Running Shoes",
      "quantity": 5,
      "price": 4500
    }
  ]
}
```

#### Response (500)

```json
{
  "error": "Server Error",
  "message": "Product out of stock",
  "timestamp": "2026-03-16T04:43:51.078200005",
  "status": 500
}
```

#### Status Codes

| Code | Description                           |
| ---- | ------------------------------------- |
| 200  | Payment processed successfully        |
| 401  | Unauthorized                          |
| 404  | Order not found                       |
| 500  | Payment failed / Product out of stock |

---

# Get My Orders

Returns all orders placed by the authenticated user.

#### Endpoint

GET /api/orders

#### Authorization

Bearer Token (JWT)

| Parameter | Type | Description            |
| --------- | ---- | ---------------------- |
| None      | -    | No parameters required |

Example Request URL

/api/orders

```bash
cURL Example
curl -X GET "http://localhost:8080/api/orders" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
[
  {
    "id": 1,
    "userId": 2,
    "totalAmount": 347500,
    "orderDate": "2026-03-16T04:39:15.110858Z",
    "paymentStatus": "SUCCESS",
    "orderStatus": "PLACED",
    "items": [
      {
        "productId": 1,
        "productName": "iPhone 13",
        "quantity": 5,
        "price": 65000
      },
      {
        "productId": 3,
        "productName": "Nike Running Shoes",
        "quantity": 5,
        "price": 4500
      }
    ]
  }
]
```

#### Status Codes

| Code | Description                   |
| ---- | ----------------------------- |
| 200  | Orders retrieved successfully |
| 401  | Unauthorized                  |
| 500  | Internal server error         |

---

# Get Order By ID

Returns details of a specific order.

#### Endpoint

GET /api/orders/{orderId}

#### Authorization

Bearer Token (JWT)

| Parameter | Type | Description     |
| --------- | ---- | --------------- |
| orderId   | Long | ID of the order |

Example Request URL

/api/orders/2

```bash
cURL Example
curl -X GET "http://localhost:8080/api/orders/2" \
 -H "Authorization: Bearer <JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
{
  "id": 2,
  "userId": 1,
  "totalAmount": 207000,
  "orderDate": "2026-03-16T04:41:01.812594Z",
  "paymentStatus": "PENDING",
  "orderStatus": "PENDING_PAYMENT",
  "items": [
    {
      "productId": 3,
      "productName": "Nike Running Shoes",
      "quantity": 46,
      "price": 4500
    }
  ]
}
```

#### Status Codes

| Code | Description                  |
| ---- | ---------------------------- |
| 200  | Order retrieved successfully |
| 401  | Unauthorized                 |
| 404  | Order not found              |
| 500  | Internal server error        |

---

# Update Order Status (Admin)

Allows an admin to update the status of an order.

#### Endpoint

PUT /api/orders/{orderId}/status

#### Authorization

Bearer Token (Admin JWT)

| Parameter | Type   | Description                                                   |
| --------- | ------ | ------------------------------------------------------------- |
| orderId   | Long   | ID of the order                                               |
| status    | String | Updated order status (Example: SHIPPED, DELIVERED, CANCELLED) |

Example Request URL

/api/orders/2/status?status=SHIPPED

```bash
cURL Example
curl -X PUT "http://localhost:8080/api/orders/2/status?status=SHIPPED" \
 -H "Authorization: Bearer <ADMIN_JWT_TOKEN>" \
 -H "Content-Type: application/json"
```

#### Response (200)

```json
{
  "id": 2,
  "userId": 3,
  "totalAmount": 217500,
  "orderDate": "2026-03-16T06:44:12.186737Z",
  "paymentStatus": "SUCCESS",
  "orderStatus": "SHIPPED",
  "items": [
    {
      "productId": 2,
      "productName": "Nike Running Shoes",
      "quantity": 5,
      "price": 4500
    },
    {
      "productId": 1,
      "productName": "iPhone 13",
      "quantity": 3,
      "price": 65000
    }
  ]
}
```

#### Status Codes

| Code | Description                       |
| ---- | --------------------------------- |
| 200  | Order status updated successfully |
| 401  | Unauthorized                      |
| 403  | Forbidden (Admin only)            |
| 404  | Order not found                   |
| 500  | Internal server error             |

---

## Test coverage (summary — corrected alignment)

I reformatted your coverage summary into a clear **Total** summary (the full detailed HTML report / Jacoco artifacts should be included in the submission as `jacoco-report/` or `test-coverage-report.txt`):

**Total coverage (project)**

* **Instructions:** `500` missed of `3,609` total → **86%** instruction coverage
* **Branches:** `29` missed of `68` total → **57%** branch coverage
* (Full Jacoco HTML report included in submission — open `jacoco-report/index.html` for line-by-line package/class data.)
  
---

## Database schema (entities & columns)

### `users` (User)

* `id` (PK)
* `name`
* `email` (unique)
* `password` (hashed with BCrypt)
* `role` (ENUM: `ROLE_ADMIN`, `ROLE_CUSTOMER`)
* `created_at`, `updated_at` (timestamps if implemented)

### `products` (Product)

* `id` (PK)
* `name`
* `description`
* `price` (decimal)
* `stock` (int)
* `category` (varchar)
* `image_url` (varchar)
* `rating` (decimal)
* timestamps

### `cart`

* `id` (PK)
* `user_id` (FK → users.id)
* `total_price` (decimal)

### `cart_item`

* `id` (PK)
* `cart_id` (FK → cart.id)
* `product_id` (FK → products.id)
* `quantity` (int)
* `subtotal` (decimal)

### `orders`

* `id` (PK)
* `user_id` (FK → users.id)
* `total_amount` (decimal)
* `order_date` (timestamp)
* `payment_status` (ENUM: `PENDING`, `SUCCESS`, `FAILED`)
* `order_status` (ENUM: `PENDING_PAYMENT`, `PLACED`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `PAYMENT_FAILED`)
* `payment_mode` (ENUM: `COD`, `ONLINE`)

### `order_item`

* `id` (PK)
* `order_id` (FK → orders.id)
* `product_id` (FK → products.id)
* `quantity` (int)
* `price` (decimal)

---
```mermaid
erDiagram
    users {
        int id PK
        string name
        string email
        string password
        enum role
    }

    products {
        int id PK
        string name
        string description
        bigdecimal price
        int stock
        string category
        string imageUrl
        double rating
    }

    cart {
        int id PK
        int user_id FK
        double total_price
    }

    cart_item {
        int id PK
        int cart_id FK
        int product_id FK
        int quantity
        double subtotal
    }

    orders {
        int id PK
        int user_id FK
        double total_amount
        datetime order_date
    }

    order_item {
        int id PK
        int order_id FK
        int product_id FK
        int quantity
        bigdecimal price
    }

    users ||--|| cart : owns
    users ||--o{ orders : places
    cart ||--o{ cart_item : contains
    products ||--o{ cart_item : referenced
    orders ||--o{ order_item : includes
    products ||--o{ order_item : referenced
```
---


## Author / Contact

**Chittaranjan Mohanty**
GitHub: `https://github.com/CHITTARANJANMOHANTY-2003/ecommerce-backend`

---
