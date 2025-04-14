# Task Management System

A cloud-native task management tool built with Spring Boot, React, and Telegram Bot integration.

## Overview

This project provides a comprehensive task management system with:

- **Backend**: Java Spring Boot REST API
- **Frontend**: React/TypeScript web application
- **Telegram Bot**: Task management through Telegram messages
- **Cloud Infrastructure**: Oracle Cloud Infrastructure (OCI) deployment
- **DevOps**: Nix build system, Docker packaging, Kubernetes deployment

## API Docs

API Endpoints and functionalities are currently documented on `http://localhost:8080/swagger-ui.html`. Feel free to check it out.

## Architecture

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│  React      │      │  Spring     │      │  Oracle     │
│  Frontend   │◄────►│  Backend    │◄────►│  Database   │
└─────────────┘      └──────┬──────┘      └─────────────┘
                           │
                     ┌─────▼──────┐
                     │  Telegram  │
                     │  Bot       │
                     └────────────┘
```

## Features

- Create, read, update, and delete tasks
- Organize tasks by projects and sprints
- Track task dependencies
- Manage tasks via Telegram Bot
- User authentication and authorization

## Prerequisites

### For local development

- Java 11+
- Node.js 16+
- Maven
- Nix (optional but recommended)

## Environment Setup

Create a `.env` file in the project root with the following variables:

```
# Telegram Bot Configuration
telegram_token=your_telegram_bot_token
telegram_name=your_bot_name

# Database Configuration
db_tns_name=your_tns_name
db_user=TODOUSER
dbpassword=your_secure_password
driver_class_name=oracle.jdbc.OracleDriver
```

## Build Instructions

### Backend (Java Spring Boot)

Using Maven:

```bash
cd MtdrSpring/backend
mvn clean install
# The compiled frontend must be in target/frontend/
mvn spring-boot:run
```

Using Nix:

```bash
cd MtdrSpring/backend
nix build .#todoapp
```

### Frontend (React)

```bash
cd MtdrSpring/front
npm install
npm run dev
```

### Docker Image

Using Nix:

```bash
nix build .#dockerImage
```

## Deployment

### Local Development

With Nix:

```bash
nix run .#todoapp
```

Without Nix:

- for \*nix systems: `bin/build.sh`
- for Windows: `bin/build.ps1`

### Kubernetes Deployment

The Kubernetes configuration is located at `MtdrSpring/backend/todolistapp-springboot.yaml`.

## Project Structure

- `MtdrSpring/backend/`: Spring Boot application

  - `src/main/java/com/springboot/MyTodoList/`: Java source code
  - `src/main/resources/`: Application properties
  - `todolistapp-springboot.yaml`: Kubernetes configuration

- `MtdrSpring/front/`: React frontend

  - `src/`: TypeScript source code
  - `public/`: Static assets

- `MtdrSpring/terraform/`: Infrastructure as Code
  - OCI resource definitions

## License

See the [LICENSE.txt](LICENSE.txt) file for details.

## Contributing

See the [CONTRIBUTING.md](CONTRIBUTING.md) file for details.
