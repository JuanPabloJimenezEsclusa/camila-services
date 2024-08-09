# camila-orchestrator

This project provides Infrastructure as Code (IaC) configurations to deploy the service in containerized and cloud environments.

## Prerequisites

* docker-compose >= v2.27.0 (or embedded in Docker)
* Kubernetes >= 1.30.0
* Linux operating system

## Environments

| Environment                         | Description                                                                        |
|-------------------------------------|------------------------------------------------------------------------------------|
| [DEV (Compose)](./dev/compose/)     | Basic container orchestration with `docker-compose` with all service components    |
| [INT (K8s - Kind)](./int/k8s/kind/) | Orchestration in `k8s` and `Kind` using either deployments or serverless `knative` |
| [INT (K8s - AWS)](./int/k8s/aws/)   | Orchestration in `k8s` and `AWS EKS`                                               |
| [PRE (AWS)](./pre/aws/)             | Orchestration in `AWS` using `AWS CloudFormation` or `Terraform`                   |

## Architecture

```txt
📦camila-orchestrator
 ┣ 📂dev
 ┃ ┗ 📂compose
 ┣ 📂int
 ┃ ┗ 📂k8s
 ┃   ┣ 📂Kind
 ┃   ┃ ┣ 📂clusters
 ┃   ┃ ┣ 📂API
 ┃   ┃ ┗ 📂DDBB
 ┃   ┗ 📂AWS
 ┃     ┗ 📂cloudformation
 ┗ 📂pre
   ┗ 📂aws
     ┣ 📂cloudformation
     ┗ 📂terraform
```
