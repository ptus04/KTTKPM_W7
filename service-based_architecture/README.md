# Mini Food Ordering - Service-Based Architecture

## Default Accounts

- Admin:
    - username: `admin`
    - password: `admin123`

## Main APIs

User Service:

- `POST /register`
- `POST /login`
- `GET /users`

Food Service:

- `GET /foods`
- `GET /foods/{id}`
- `POST /foods` (ADMIN)
- `PUT /foods/{id}` (ADMIN)
- `DELETE /foods/{id}` (ADMIN)

Order Service:

- `POST /orders`
- `GET /orders`
- `PUT /orders/{id}/status`

Payment Service:

- `POST /payments`

Notification Service:

- `POST /notifications`
