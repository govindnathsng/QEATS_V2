# QEats

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

> **Date:** July 2023

## 📘 Overview
**QEats** is a popular food ordering app enabling users to browse and order dishes from nearby restaurants. This project involves implementing various backend features, resolving production issues, optimizing app performance, and adding advanced search capabilities.

During this project:
- Built core backend functionalities of QEats, a Spring Boot application.
- Developed several REST API endpoints for restaurant information and food ordering.
- Investigated and resolved production issues using Scientific Debugging methods.
- Enhanced app performance under high load and implemented an advanced search feature.

## 🛠️ Project Structure

### **Retrieve Restaurant Data**
#### Scope of Work
- Implemented the `GET /API/v1/restaurants` endpoint along with request handler and response methods.
- Leveraged **Mockito** for developing MVCS layers independently.
- Retrieved a list of restaurants based on user location from **MongoDB**.

#### Skills Used
- **Spring Boot**, **Spring Data**, **REST API**, **Jackson**, **Mockito**, **JUnit**, **MongoDB**

---

### **Resolve Production Issues Using Scientific Debugging**
#### Scope of Work
- Used structured debugging techniques and log messages to identify and fix QEats app crashes.
- Applied **breakpoints** and **assert statements** to trace and resolve root causes.

#### Skills Used
- **Scientific Debugging**

---

### **Replicate and Solve Performance Issues Using Caching Strategies**
#### Scope of Work
- Conducted load testing with **JMeter** to identify performance bottlenecks.
- Analyzed DB queries impacting performance and utilized **Redis caching** to improve read speeds.

#### Skills Used
- **Redis**, **JMeter**

---

### **Perform Search Operations with Custom Attributes**
#### Scope of Work
- Enabled user search for restaurants by attributes like name, cuisine, dish, and price using **MongoDB queries**.
- Enhanced concurrent search capabilities using **multithreading**.

#### Skills Used
- **MongoDB querying**, **Multithreading**

---

## 🚀 Getting Started

1. **Clone the Repository**
   ```bash
   git clone https://github.com/govindnathsng/QEats.git
   cd QEats

    Build the Project

    bash

./gradlew build

Run the Tests

bash

    ./gradlew test

⚙️ Technologies & Tools

    Backend: Java, Spring Boot, MongoDB, Redis
    Testing: JUnit, Mockito, JMeter
    Debugging: Scientific Debugging, IDE Breakpoints, Log Analysis
    Concurrency: Multithreading for optimized search performance

📈 Features

    Restaurant Retrieval: Fetch restaurants based on user location.
    Search Functionality: Search by name, cuisine, dish, and price.
    Error Handling: Debug and resolve production issues.
    Performance Optimization: Redis caching for faster data retrieval.

🤝 Contributing

Contributions are welcome! Fork the repository and submit a pull request to add new features or enhance the existing code.
📄 License

This project is licensed under the Apache License 2.0.
📞 Contact

For questions or feedback, feel free to reach out:

    GitHub: govindnathsng
    Email: govindnathsng@gmail.com

Check out my <a href="https://www.crio.do/learn/portfolio/govindnathsng/" target="_blank">Crio Portfolio</a> for more projects like this!


