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
| Drive / submission | Google Drive                |

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

## Full API documentation (each endpoint, auth & example)

> Replace `{{baseUrl}}` with `http://localhost:8080`. For protected endpoints include header `Authorization: Bearer <JWT>`.

### AUTH / USER

#### `POST /api/users/register`

* **Auth:** public
* **Body**

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

* **Response (201)** — created user DTO

#### `POST /api/users/login`

* **Auth:** public
* **Body**

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

* **Response (200)** — example:

```json
{
  "token": "eyJhbGciOiJI...",
  "tokenType": "Bearer",
  "expiresIn": 3600000
}
```

> Use this token in `Authorization` header for protected calls.

#### `GET /api/users/me`

* **Auth:** `ROLE_CUSTOMER` or `ROLE_ADMIN`
* **Header:** `Authorization: Bearer <JWT>`
* **Description:** returns profile for the current authenticated user.

#### `PUT /api/users/me`

* **Auth:** `ROLE_CUSTOMER` or `ROLE_ADMIN`
* Update user profile.

#### `PUT /api/users/me/password`

* **Auth:** `ROLE_CUSTOMER` or `ROLE_ADMIN`
* Change password (provide old and new passwords as per API DTO).

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
