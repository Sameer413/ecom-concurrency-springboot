-- ========== USERS ==========
CREATE TABLE users (
id              BIGSERIAL PRIMARY KEY,
email           VARCHAR(255) UNIQUE NOT NULL,
password_hash   VARCHAR(255) NOT NULL,
first_name      VARCHAR(100),
last_name       VARCHAR(100),
phone           VARCHAR(20),
role            VARCHAR(20) DEFAULT 'customer', -- customer, admin, vendor
created_at      TIMESTAMPTZ DEFAULT now(),
updated_at      TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE addresses (
id              BIGSERIAL PRIMARY KEY,
user_id         BIGINT REFERENCES users(id) ON DELETE CASCADE,
label           VARCHAR(50),          -- home, work, etc.
line1           VARCHAR(255) NOT NULL,
line2           VARCHAR(255),
city            VARCHAR(100) NOT NULL,
state           VARCHAR(100),
postal_code     VARCHAR(20),
country         VARCHAR(2) NOT NULL,  -- ISO code
is_default      BOOLEAN DEFAULT FALSE
);

-- ========== CATALOG ==========

CREATE TABLE products (
id              BIGSERIAL PRIMARY KEY,
category_id     BIGINT REFERENCES categories(id), 
name            VARCHAR(255) NOT NULL,
slug            VARCHAR(255) UNIQUE NOT NULL,
description     TEXT,
brand           VARCHAR(100),
status          VARCHAR(20) DEFAULT 'active', -- active, draft, archived
base_price      NUMERIC(12,2) NOT NULL,
currency        CHAR(3) DEFAULT 'USD',
created_at      TIMESTAMPTZ DEFAULT now(),
updated_at      TIMESTAMPTZ DEFAULT now()
);

-- Variants (size/color/etc.) — each is the sellable unit
CREATE TABLE product_variants (
id              BIGSERIAL PRIMARY KEY,
product_id      BIGINT REFERENCES products(id) ON DELETE CASCADE,
sku             VARCHAR(100) UNIQUE NOT NULL,
attributes      JSONB,               -- {"color":"red","size":"M"}
price           NUMERIC(12,2) NOT NULL,
weight_grams    INTEGER,
is_active       BOOLEAN DEFAULT TRUE
);

-- ========== INVENTORY ==========
CREATE TABLE inventory (
id              BIGSERIAL PRIMARY KEY,
variant_id      BIGINT REFERENCES product_variants(id) ON DELETE CASCADE,
quantity_on_hand    INTEGER NOT NULL DEFAULT 0,
quantity_reserved   INTEGER NOT NULL DEFAULT 0, -- held by active reservations
UNIQUE(variant_id, warehouse_id)
);

-- ========== RESERVATIONS (stock holds) ==========
-- Temporarily locks stock while a user is checking out or has an active cart,
-- preventing overselling before payment completes.
CREATE TABLE reservations (
id              BIGSERIAL PRIMARY KEY,
variant_id      BIGINT REFERENCES product_variants(id),
cart_id         BIGINT REFERENCES carts(id),
order_id        BIGINT REFERENCES orders(id),   -- set once converted to an order
quantity        INTEGER NOT NULL CHECK (quantity > 0),
status          VARCHAR(20) DEFAULT 'active',   -- active, expired, released, fulfilled
expires_at      TIMESTAMPTZ NOT NULL,
created_at      TIMESTAMPTZ DEFAULT now()
);

-- ========== CART ==========
CREATE TABLE carts (
id              BIGSERIAL PRIMARY KEY,
user_id         BIGINT REFERENCES users(id),
session_id      VARCHAR(100),        -- for guest carts
status          VARCHAR(20) DEFAULT 'open', -- open, converted, abandoned
created_at      TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE cart_items (
id              BIGSERIAL PRIMARY KEY,
cart_id         BIGINT REFERENCES carts(id) ON DELETE CASCADE,
variant_id      BIGINT REFERENCES product_variants(id),
quantity        INTEGER NOT NULL CHECK (quantity > 0),
unit_price      NUMERIC(12,2) NOT NULL, -- price snapshot at add-time
UNIQUE(cart_id, variant_id)
);

-- ========== ORDERS ==========
CREATE TABLE orders (
id              BIGSERIAL PRIMARY KEY,
user_id         BIGINT REFERENCES users(id),
cart_id         BIGINT REFERENCES carts(id),
order_number    VARCHAR(50) UNIQUE NOT NULL,
status          VARCHAR(20) DEFAULT 'pending', -- pending, paid, shipped, delivered, cancelled, refunded
shipping_address_id BIGINT REFERENCES addresses(id),
billing_address_id  BIGINT REFERENCES addresses(id),
subtotal        NUMERIC(12,2) NOT NULL,
discount_total  NUMERIC(12,2) DEFAULT 0,
tax_total       NUMERIC(12,2) DEFAULT 0,
shipping_total  NUMERIC(12,2) DEFAULT 0,
grand_total     NUMERIC(12,2) NOT NULL,
placed_at       TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE order_items (
id              BIGSERIAL PRIMARY KEY,
order_id        BIGINT REFERENCES orders(id) ON DELETE CASCADE,
variant_id      BIGINT REFERENCES product_variants(id),
quantity        INTEGER NOT NULL,
unit_price      NUMERIC(12,2) NOT NULL,
line_total      NUMERIC(12,2) NOT NULL
);

CREATE TABLE payments (
id              BIGSERIAL PRIMARY KEY,
order_id        BIGINT REFERENCES orders(id),
provider        VARCHAR(50),        -- stripe, paypal, razorpay...
provider_ref    VARCHAR(255),
amount          NUMERIC(12,2) NOT NULL,
status          VARCHAR(20),        -- authorized, captured, failed, refunded
created_at      TIMESTAMPTZ DEFAULT now()
);

-- ========== EXTRAS ==========
CREATE TABLE reviews (
id              BIGSERIAL PRIMARY KEY,
product_id      BIGINT REFERENCES products(id),
user_id         BIGINT REFERENCES users(id),
rating          SMALLINT CHECK (rating BETWEEN 1 AND 5),
comment         TEXT,
created_at      TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE coupons (
id              BIGSERIAL PRIMARY KEY,
code            VARCHAR(50) UNIQUE NOT NULL,
discount_type   VARCHAR(10),        -- percent, fixed
discount_value  NUMERIC(12,2),
valid_from      TIMESTAMPTZ,
valid_until     TIMESTAMPTZ,
usage_limit     INTEGER,
times_used      INTEGER DEFAULT 0
);