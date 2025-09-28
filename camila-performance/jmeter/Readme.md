# Jmeter

> [Summary](#-summary)
  • [Dependencies](#-dependencies)
  • [Architecture](#-architecture)
  • [Usage](#-usage)

## 📜 Summary

---

Performance testing project using JMeter

## ⚙️ Dependencies

---

* JDK ~= [25.x](https://openjdk.org/projects/jdk/25/)
* JMeter ~= [5.6.x](https://jmeter.apache.org/download_jmeter.cgi)

## 🏗️ Architecture

---

| File                   | Description                         |
|------------------------|-------------------------------------|
| camila-product-api.jmx | Test plan file                      |
| run.sh                 | Shell script for executing the test |

## 🛠️ Usage

---

```bash
JMETER_TEST_PATH="." THREADS=500 RAMP_UP=20 LOOPS=10 \
BASE_URL_PROTOCOL="http" BASE_URL="localhost" BASE_URL_PORT="8090" BASE_PATH="product-dev" \
OAUTH_URL_PROTOCOL="http" OAUTH_URL="localhost" OAUTH_URL_PORT="9191" OAUTH_PATH="/realms/camila-realm/protocol/openid-connect/token" \
OAUTH_GRANT_TYPE="client_credentials" OAUTH_SCOPE="camila/read camila/write" \
OAUTH_CLIENT_ID="camila-client" OAUTH_CLIENT_SECRET="Fuvf8XyBDXxU57NAOOFZVvdUIPmGgiyE" \
 ./run.sh
```
