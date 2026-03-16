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

Below is the **Markdown documentation strictly following your stored template structure** (###, ####, tables, and fenced code blocks). You can **paste this directly into your `README.md`**.

---

# Admin Management APIs

---

### Create Admin

Creates a new administrator account.

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


### PRODUCTS (public & admin)

#### `GET /api/products`

* **Auth:** public
* **Query params:** `page`, `size`, `category`, `minPrice`, `maxPrice`, `keyword`, `sort`
* **Example:**
  `GET /api/products?category=Electronics&minPrice=1000&maxPrice=5000&page=0&size=10`

#### `GET /api/products/{id}`

* **Auth:** public

#### `GET /api/products/search?keyword=...`

* **Auth:** public

#### `POST /api/products` (admin)

* **Auth:** `ROLE_ADMIN`
* **Header:** `Authorization: Bearer <JWT>`
* Create new product (send product DTO: name, description, price, stock, category, imageUrl, rating).

#### `PUT /api/products/{id}` (admin)

#### `DELETE /api/products/{id}` (admin)

---

### CART

All cart endpoints require `ROLE_CUSTOMER` or `ROLE_ADMIN` (JWT header).

* `GET /api/cart` — get current user cart
* `POST /api/cart/add/{productId}?quantity=` — add item
* `PUT /api/cart/update/{productId}?quantity=` — update item quantity
* `DELETE /api/cart/remove/{productId}` — remove item
* `DELETE /api/cart/clear` — clear cart

---

### ORDERS

* `POST /api/orders/checkout?paymentMode=COD`
  **Auth:** `ROLE_CUSTOMER` or `ROLE_ADMIN`
  Converts cart → order; order stored with `PENDING_PAYMENT` (or other status based on payment mode).

* `POST /api/orders/{orderId}/pay?success=true|false`
  **Auth:** `ROLE_CUSTOMER` or `ROLE_ADMIN`
  Query param `success`: `true` → payment succeeded, `false` → payment failed. Order status and payment status updated accordingly.

* `GET /api/orders` — list current user orders (auth required)

* `GET /api/orders/{id}` — order details (user only for own orders)

* `PUT /api/orders/{id}/status?status=SHIPPED` — **admin only**; allowed statuses: `PLACED`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `PAYMENT_FAILED`.

---

## Test coverage (summary — corrected alignment)

I reformatted your coverage summary into a clear **Total** summary (the full detailed HTML report / Jacoco artifacts should be included in the submission as `jacoco-report/` or `test-coverage-report.txt`):

**Total coverage (project)**

* **Instructions:** `500` missed of `3,609` total → **86%** instruction coverage
* **Branches:** `29` missed of `68` total → **57%** branch coverage
* (Full Jacoco HTML report included in submission — open `jacoco-report/index.html` for line-by-line package/class data.)

> If you want the full per-package table re-aligned into columns for every package I can format it precisely from the Jacoco text/CSV — include the raw `jacoco` summary output and I’ll reformat it exactly. In your submission include the full `jacoco` folder so the grader can inspect interactive HTML.

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


## Author / Contact

**Chittaranjan Mohanty**
GitHub: `https://github.com/CHITTARANJANMOHANTY-2003/ecommerce-backend`

---
