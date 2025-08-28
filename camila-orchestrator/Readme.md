# camila-orchestrator

> [Summary](#-summary)
  • [Dependencies](#-dependencies)
  • [Architecture](#-architecture)
  • [Usage](#-usage)

## 📜 Summary

---

This project provides Infrastructure as Code (IaC) configurations to deploy the service in containerized and cloud environments.

## ⚙️ Dependencies

---

* Linux operating system

## 🏗️ Architecture

---

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

## 🛠️ Usage

---

### Environments

| Environment                         | Description                                                                        |
|-------------------------------------|------------------------------------------------------------------------------------|
| [DEV (Compose)](./dev/compose/)     | Basic container orchestration with `docker-compose` with all service components    |
| [INT (K8s - Kind)](./int/k8s/kind/) | Orchestration in `k8s` and `Kind` using either deployments or serverless `knative` |
| [INT (K8s - AWS)](./int/k8s/aws/)   | Orchestration in `k8s` and `AWS EKS`                                               |
| [PRE (AWS)](./pre/aws/)             | Orchestration in `AWS` using `AWS CloudFormation` or `Terraform`                   |
