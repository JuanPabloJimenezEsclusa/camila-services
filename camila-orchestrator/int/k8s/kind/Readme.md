# Camila Product Orchestrator (Kubernetes/Kind)

> [Summary](#-summary)
  • [Architecture](#-architecture)
  • [Dependencies](#-dependencies)
  • [Setup](#-setup)
  • [Usage Examples](#-usage-examples)
  • [Links](#-links)
  • [Troubleshooting](#-troubleshooting)
  • [Notes](#-notes)

## 📜 Summary

---

Built on [Kind](https://kind.sigs.k8s.io/) (Kubernetes IN Docker), this environment provides a local Kubernetes setup for development and testing of the Camila Product services.

## 🏗️ Architecture

---

<details>
<summary><strong>Expand Architecture</strong></summary>

This setup uses Kind to create a local Kubernetes cluster inside Docker containers, providing a lightweight development environment with all the functionality of Kubernetes.

</details>

## ⚙️ Dependencies

---

<details>
<summary><strong>Expand Dependencies</strong></summary>

* Kind ~= [0.30.0](https://kind.sigs.k8s.io/docs/user/quick-start/#installation)
* K9s ~= [v0.50.x](https://github.com/derailed/k9s/releases)

</details>

## 🚀 Setup

---

<details>
<summary><strong>Expand Setup</strong></summary>

### 📝 Hosts Configuration

Before starting the cluster, ensure your `/etc/hosts` file has the proper kind-registry entry:

### 🔄 Cluster Management

```bash
# Install required tools
cd clusters
./install.sh

# Start the Kind cluster
./start-cluster.sh

# Stop the Kind cluster
./stop-cluster.sh

# Run smoke tests
cd smoke-tests
./start.sh
./stop.sh
```

### 💾 Database Operations

```bash
# Deploy MongoDB
cd DDBB
./apply.sh

# Connect to MongoDB
mongo_pod="$(kubectl get pods -n mongodb --selector=app=mongo -o jsonpath='{.items[*].metadata.name}')"
kubectl exec -it --namespace=mongodb "${mongo_pod}" -- \
  bash -c "mongosh --host localhost:27017 \
           --username adminuser \
           --password password123 \
           --authenticationDatabase admin"

# Add test data
mongo_pod="$(kubectl get pods -n mongodb --selector=app=mongo -o jsonpath='{.items[*].metadata.name}')" && \
kubectl exec -it --namespace=mongodb "${mongo_pod}" -- \
  bash -c "mongosh --host localhost:27017 \
           --username adminuser \
           --password password123 \
           --authenticationDatabase admin" < ../../../../dev/compose/data/mongodb/minimum_data.script

# Remove MongoDB
./delete.sh
```

### 📡 API Deployment

```bash
# Deploy standard API
cd API
./apply.sh
./delete.sh

# Deploy serverless API
cd serverless
./apply-serveless.sh
./delete-serveless.sh
```

### 🔄 Full Setup

Use the combined script to set up everything at once:

```bash
# Run the full setup script
./run-cluster-with-api.sh
./run-cluster-with-api-serveless.sh
```

</details>

## 🔍 Usage Examples

---

<details>
<summary><strong>Expand Usage Examples</strong></summary>

```bash
# Test DNS resolution from inside the cluster
kubectl run -it --rm \
  --restart=Never dns-test \
  --image=busybox -- nslookup mongo-nodeport-svc.mongodb.svc.cluster.local
```

</details>

## 🌐 Links

---

<details>
<summary><strong>Expand Links</strong></summary>

- [Product API](http://localhost:8080/product-int/api/swagger-ui.html)
- [Product API (Serverless)](http://camila-product-api-serveless.camila-product-api-serveless-ns.127.0.0.1.sslip.io/product-int/api/swagger-ui/index.html)

</details>

## ❓ Troubleshooting

---

<details>
<summary><strong>Expand Troubleshooting</strong></summary>

### Registry Connection Issues

If you have problems pushing images to the local registry, verify:
- The entry in `/etc/hosts` matches the actual registry IP (should be 172.18.0.6)
- The registry container is running (`docker ps | grep kind-registry`)

### Pod DNS Resolution

Test DNS resolution within the cluster:

```bash
kubectl run -it --rm \
  --restart=Never dns-test \
  --image=busybox -- nslookup mongo-nodeport-svc.mongodb.svc.cluster.local
```

</details>

## 📝 Notes

---

<details>
<summary><strong>Expand Notes</strong></summary>

### 🔍 List Node Images

```bash
docker exec -it kind-cluster-worker bash -c "crictl images"
docker exec -it kind-cluster-worker2 bash -c "crictl images"
docker exec -it kind-cluster-worker3 bash -c "crictl images"
```

### 📦 Available Scripts

| Category       | Script                                                                 | Description                        |
|----------------|------------------------------------------------------------------------|------------------------------------|
| **Cluster**    | [clusters/install.sh](clusters/install.sh)                             | Install K8s, Helm and Kind tools   |
|                | [clusters/start-cluster.sh](clusters/start-cluster.sh)                 | Start Kind cluster                 |
|                | [clusters/stop-cluster.sh](clusters/stop-cluster.sh)                   | Stop Kind cluster                  |
| **Tests**      | [clusters/smoke-tests/start.sh](clusters/smoke-tests/start.sh)         | Start smoke tests                  |
|                | [clusters/smoke-tests/stop.sh](clusters/smoke-tests/stop.sh)           | Stop smoke tests                   |
| **Database**   | [DDBB/apply.sh](DDBB/apply.sh)                                         | Deploy MongoDB resources           |
|                | [DDBB/delete.sh](DDBB/delete.sh)                                       | Delete MongoDB resources           |
| **API**        | [API/apply.sh](API/apply.sh)                                           | Deploy API resources               |
|                | [API/delete.sh](API/delete.sh)                                         | Delete API resources               |
| **Serverless** | [API/serveless/apply-serveless.sh](API/serveless/apply-serveless.sh)   | Deploy serverless API              |
|                | [API/serveless/delete-serveless.sh](API/serveless/delete-serveless.sh) | Delete serverless API              |
| **Combined**   | [run-cluster-with-api.sh](run-cluster-with-api.sh)                     | Run full setup with standard API   |
|                | [run-cluster-with-api-serveless.sh](run-cluster-with-api-serveless.sh) | Run full setup with serverless API |

</details>
