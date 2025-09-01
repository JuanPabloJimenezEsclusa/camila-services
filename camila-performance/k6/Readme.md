# K6 Testing

> [Summary](#-summary)
  • [Dependencies](#-dependencies)
  • [Architecture](#-architecture)
  • [Usage](#-usage)

## 📜 Summary

---

Performance testing project using K6

## ⚙️ Dependencies

---

* [K6 ~0.57](https://grafana.com/docs/k6/next/release-notes/)
* [Docker ~28](https://docs.docker.com/engine/release-notes/28/)

## 🏗️ Architecture

---

| File                                                                   | Description                          |
|------------------------------------------------------------------------|--------------------------------------|
| [camila-product-api-load-tests.js](./camila-product-api-load-tests.js) | Test plan file                       |
| [run.sh](./run.sh)                                                     | Shell script for executing the tests |

## 🛠️ Usage

---

```bash
# http://localhost:5665/
THREADS=200 RAMP_UP=20 LOOPS=10 \
BASE_URL_PROTOCOL="http" BASE_URL="localhost" BASE_URL_PORT="8090" BASE_PATH="product-dev" \
OAUTH_URL_PROTOCOL="http" OAUTH_URL="localhost" OAUTH_URL_PORT="9191" OAUTH_PATH="/realms/camila-realm/protocol/openid-connect/token" \
OAUTH_GRANT_TYPE="client_credentials" OAUTH_SCOPE="camila/read camila/write" \
OAUTH_CLIENT_ID="camila-client" OAUTH_CLIENT_SECRET="Fuvf8XyBDXxU57NAOOFZVvdUIPmGgiyE" \
 ./run.sh
```
