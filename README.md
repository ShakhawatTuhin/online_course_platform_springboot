# Online Course Platform

This is a Spring Boot application for an online course platform.

## Quick Start: Login Credentials & Registration

You can log in with the following credentials:

- **Instructor:** `instructor`, password: `instructorpass`
- **Admin:** `admin`, password: `adminpass`
- **Student:** `student`, password: `studentpass`

Or, you can create any user by registering via the registration page.

## Screenshots

<p align="center">
  <img src="assets/screenshot1.png" width="45%" />
  <img src="assets/screenshot2.png" width="45%" />
</p>
<p align="center">
  <img src="assets/screenshot3.png" width="45%" />
  <img src="assets/screenshot4.png" width="45%" />
</p>
<p align="center">
  <img src="assets/screenshot5.png" width="45%" />
  <img src="assets/screenshot6.png" width="45%" />
</p>

## Project Requirements Overview

- **Backend**: Spring Boot 3.x
- **Database**: SQL database (H2 in-memory for development) with JPA.
- **Database Relations**: Must include OneToOne, OneToMany, ManyToOne, and ManyToMany relationships.
- **Web Interface**: Thymeleaf with Spring Security, a home menu, and CRUD functionality for at least one entity.
- **REST API**: CRUD interface (JSON) for all major entities.
- **Version Control**: Project hosted on GitLab.

## Prerequisites

- Java 17 or higher
- Apache Maven 3.6.x or higher

## Setup Instructions

1.  **Clone the repository:**
    ```bash
    git clone <your-gitlab-repository-url>
    cd online-course-platform
    ```

2.  **Build the project:**
    ```bash
    mvn clean install
    ```

## Running the Application

Once the project is built, you can run the application using the Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```

The application will typically be available at `http://localhost:8081`.

> **Note:** Please ensure port **8081** is open and available on your system before running the application.

## API Endpoints

- **Users**: `/api/users`
- **Courses**: `/api/courses`
- **Lessons**: `/api/lessons`

Refer to the controller classes for detailed endpoint definitions and request/response formats.

## Web Interface

- **Home Page**: `/`
- **Login**: `/login`
- **Register**: `/register`
- **Courses**: `/courses`

## Database

- The application uses an H2 in-memory database by default for development.
- The H2 console can typically be accessed at `http://localhost:8080/h2-console` (ensure the path and credentials match your `application.yml` settings).
    - **JDBC URL**: `jdbc:h2:mem:testdb` (or as configured)
    - **Username**: `sa`
    - **Password**: `password` (or as configured) 