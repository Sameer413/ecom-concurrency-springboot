src/main/java/com/example/flashsale/
│
├── FlashSaleApplication.java
│
├── common/
│   ├── config/
│   ├── exception/
│   ├── response/
│   ├── security/
│   └── util/
│
├── product/
│   ├── controller/
│   │   └── ProductController.java
│   ├── service/
│   │   └── ProductService.java
│   ├── repository/
│   │   └── ProductRepository.java
│   ├── entity/
│   │   └── Product.java
│   ├── dto/
│   │   ├── CreateProductRequest.java
│   │   └── ProductResponse.java
│   └── ProductModule.java
│
├── inventory/
│   ├── controller/
│   ├── service/
│   │   └── InventoryService.java
│   ├── repository/
│   │   └── InventoryRepository.java
│   ├── entity/
│   │   └── Inventory.java
│   ├── dto/
│   └── InventoryModule.java
│
├── reservation/
│   ├── controller/
│   ├── service/
│   │   └── ReservationService.java
│   ├── repository/
│   │   └── ReservationRepository.java
│   ├── entity/
│   │   └── Reservation.java
│   ├── dto/
│   └── ReservationModule.java
│
├── payment/
│   ├── controller/
│   ├── service/
│   ├── client/
│   ├── dto/
│   └── PaymentModule.java
│
├── order/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── OrderModule.java
│
├── notification/
│   ├── service/
│   ├── client/
│   └── NotificationModule.java
│
└── infrastructure/
├── redis/
│   ├── RedisConfig.java
│   └── RedisService.java
├── kafka/
│   ├── KafkaConfig.java
│   ├── KafkaProducer.java
│   └── KafkaConsumer.java
└── database/
└── DatabaseConfig.java