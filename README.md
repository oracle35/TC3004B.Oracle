# TC3004B

## Task Management System for Oracle

### How to run the project

Use:

```bash
cd MtdrSpring/backend && mvn clean install && mvn spring-boot:run
```

The project will be available in `http://localhost:8081/` after around 1-2 minutes.

### Frontend

> A current new frontend version is being worked on, currently in the folder `/frontend-v2`.

The frontend is simply a wrapper to do requests to the backend, it just makes requests to the `todolist/` endpoint.
It is extensively documented in `./FRONTEND.md`.

Please be sure to read the **setup** part next in this repository in order to know how to set up this project.

## MyToDo React JS

The `mtdrworkshop` repository hosts the materiald (code, scripts and instructions) for building and deploying Cloud Native Application using a Java/Helidon backend

### Requirements

The lab executes scripts that require the following software to run properly: (These are already installed on and included with the OCI Cloud Shell)

* oci-cli
* python 2.7^
* terraform
* kubectl
* mvn (maven)
