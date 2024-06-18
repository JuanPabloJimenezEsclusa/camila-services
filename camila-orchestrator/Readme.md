# camila-orchestrator

This project provides Infrastructure as Code (IaC) configurations to deploy the service in containerized and cloud environments.

## Prerequisites

* docker-compose >= v2.27.0 (or embedded in Docker)
* Kubernetes >= 1.30.0
* Linux operating system

## Environments

| Environment   | Description                                                                                                        |
|---------------|--------------------------------------------------------------------------------------------------------------------|
| [DEV](./dev/) | Basic container orchestration with `docker-compose` to create a deployment environment with all service components |
| [INT](./int/) | Orchestration in `k8s` using either deployments or serverless `knative`                                            |
| [PRE](./pre/) | Orchestration in `AWS` using `AWS CloudFormation` or `Terraform`                                                   |

## Infrastructure

| Provider                            | Description                                                                                           |
|-------------------------------------|-------------------------------------------------------------------------------------------------------|
| [clusters - kind](./clusters/kind/) | Configuration of a `k8s` cluster using the project: [kubernetes in docker](https://kind.sigs.k8s.io/) |

## Architecture

```txt
📦camila-orchestrator
 ┣ 📂clusters
 ┃ ┗ 📂kind (install.sh - start-cluster.sh - stop-cluster.sh)
 ┣ 📂dev
 ┃ ┗ 📂compose (start.sh - stop.sh)
 ┣ 📂int
 ┃ ┗ 📂k8s
 ┃   ┣ 📂API
 ┃   ┃ ┗ 📂serveless (apply-serveless.sh - delete-serveless.sh)
 ┃   ┗ 📂DDBB (apply.sh - delete.sh)
 ┗ 📂pre
   ┗ 📂aws
     ┣ 📂cloudformation
     ┗ 📂terraform
```
