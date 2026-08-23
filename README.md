<<<<<<< HEAD
# Hiring4U-Version-03
=======
# 🚀 Hiring4U – Deployable Job Service

Hiring4U is a role-based job portal built with Spring Boot and MySQL. It now includes a responsive candidate job experience, a safe public jobs API, Docker deployment assets, health checks, and an EC2 deployment workflow.

---

## 🔥 Features

### 👤 Candidate
- Register account
- Login using Basic Authentication
- View all available jobs
- Apply for jobs
- Search, save and review roles through the responsive web UI

### 🏢 Recruiter
- Register company account
- Login using Basic Authentication
- Post new jobs
- View all posted jobs

### 🔐 Security
- Spring Security with Basic Authentication
- Role-Based Access Control (ADMIN / RECRUITER / CANDIDATE)
- BCrypt Password Encryption
- Secure endpoints with role authorization

---

## 🛠 Tech Stack

- Java 17+
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- Spring Boot Actuator
- Docker Compose + Nginx
- GitHub Actions
- Maven
- IntelliJ IDEA
- Postman (API Testing)

---

## 📂 Project Structure

```
com.hiring4u
│
├── controller
├── service
├── repository
├── entity
├── dto
├── security
└── enums
```

---

## 🗄 Database Tables

- candidates
- recruiters
- jobs
- applications

Relationship:
- One Recruiter → Many Jobs
- One Candidate → Many Applications

---

## ⚙️ Setup Instructions

### 1️⃣ Clone Repository

```bash
git clone https://github.com/your-username/hiring4u.git
cd hiring4u
```

### 2️⃣ Run locally

The application uses MySQL by default. Create the database once, then provide your local MySQL credentials:

```bash
mysql -u root -p -e 'CREATE DATABASE IF NOT EXISTS job_portal'
DB_USERNAME=root DB_PASSWORD='your-password' ./mvnw spring-boot:run
```

Application runs on:
```
http://localhost:8080
```

The application reads and writes candidates, recruiters, job posts, applications, and password reset tokens from this database. To use a different database name or host, set `DB_URL`:

```bash
DB_URL='jdbc:mysql://localhost:3306/hiring4u?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
DB_USERNAME=root DB_PASSWORD='your-password' ./mvnw spring-boot:run
```

For the optional local H2 profile, use `SPRING_PROFILES_ACTIVE=local`.

## 🐳 Run with Docker

```bash
cp .env.example .env
# Replace the example passwords in .env
docker compose up -d --build
```

The responsive UI and API are then available at `http://localhost`. Verify readiness at `http://localhost/actuator/health`.

## ☁️ AWS EC2 deployment

This repository is prepared to deploy as the independently deployable **job-service**. A full microservice fleet is deliberately not claimed yet—the next natural split is an `application-service` once the API contract is versioned and services communicate over an event broker.

1. Launch an Amazon Linux 2023 EC2 instance and allow inbound TCP `80` only (and `22` only from your IP).
2. Run `deployment/ec2/bootstrap-amazon-linux.sh` as the initial EC2 user, then reconnect so Docker group membership applies.
3. Clone this repository into `/opt/hiring4u`, copy `.env.example` to `.env`, and use strong unique database passwords.
4. Run `docker compose up -d --build`, then check `curl http://localhost/actuator/health` on the instance.
5. For HTTPS, put the EC2 instance behind an Application Load Balancer with an ACM certificate. Do not expose MySQL (`3306`) publicly.

### CI/CD secrets

The verification workflow runs tests and validates the Docker image. To enable `.github/workflows/deploy-ec2.yml`, configure these GitHub environment secrets:

- `EC2_HOST` – public IP or DNS name
- `EC2_USER` – SSH user, normally `ec2-user`
- `EC2_SSH_KEY` – private deployment key

The EC2 deployment workflow is intentionally inactive until those secrets exist; it rebuilds the isolated `job-service` and restarts it with Docker Compose.

---

## 📌 API Endpoints

### Candidate

| Method | Endpoint | Description |
|--------|----------|------------|
| POST | /can/registered | Register Candidate |
| GET | /candidate/jobs | View Jobs |

### Recruiter

| Method | Endpoint | Description |
|--------|----------|------------|
| POST | /rec/registered | Register Recruiter |
| POST | /recruiter/post-job | Post Job |
| GET | /recruiter/home | Recruiter Dashboard |

---

## 🔐 Authentication

Basic Auth is required for secured endpoints.

Use:
- Username = Email
- Password = Registered password

---

## 📈 Future Improvements

- JWT Authentication
- Resume Upload
- Pagination
- Job Search Filters
- Admin Dashboard
- Docker Deployment

---

## 👨‍💻 Author

Md Arif  
Electronics & Communication Engineer  
Java Backend Developer  

---

## ⭐ Contribution

Contributions are welcome.  
Feel free to fork and raise pull requests.

---

## 📜 License

This project is for educational and learning purposes.
>>>>>>> f82bb48 (Version deploying)
