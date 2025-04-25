# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build/Run Commands
- **Backend**: 
  - Maven: `cd MtdrSpring/backend && mvn clean install && mvn spring-boot:run`
  - Nix: `cd MtdrSpring/backend && nix build .#todoapp`
- **Frontend**: `cd MtdrSpring/front && npm run dev`
- **Lint**: 
  - Frontend: `cd MtdrSpring/front && npm run lint`
- **Tests**: 
  - Backend: `cd MtdrSpring/backend && mvn test`
  - Frontend: Not configured yet

## Deployment
- **Docker**: `nix build .#dockerImage` (on nix branch)
- **Kubernetes**: Config in `MtdrSpring/backend/todolistapp-springboot.yaml`
- **Terraform**: Infrastructure defined in `MtdrSpring/terraform/`

## Code Style Guidelines
- **Backend**: Java 11 Spring Boot application
  - Standard Java naming conventions (camelCase for methods/variables, PascalCase for classes)
  - Repository/Service/Controller architecture pattern
  - Package structure: `com.springboot.MyTodoList.*`
  
- **Frontend**: React/TypeScript with Vite
  - TypeScript strict mode enabled
  - ESLint for code quality (see eslint.config.js)
  - React component files use `.tsx` extension
  - Model interfaces in `/models` directory
  - API services in `/api` directory
  - Material UI and Tailwind CSS for styling