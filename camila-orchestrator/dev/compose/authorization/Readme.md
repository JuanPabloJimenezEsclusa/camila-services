# orchestrator-dev (authz)

> [Summary](#-summary)
  • [Links](#-links)
  • [Usage](#-usage)
  • [Notes](#-notes)

## 📜 Summary

---

To configure `keycloak`, the file [camila-realm-realm.json](camila-realm-realm.json) is imported. It contains:

* **realm**: `camila-realm`
* **scopes**: `camila/read`, `camila/write`
* **client**: (id) `camila-client` (secret) `Fuvf8XyBDXxU57NAOOFZVvdUIPmGgiyE`
* **user**: `camila/camila`

> In this configuration, the hostnames `keycloak` and `gateway` are used. These should be defined in the `/etc/hosts` file.

## 🔗 Links

---

* [oauth playground](https://www.oauth.com/playground)
* [local keycloak openid-configuration](http://keycloak:9191/realms/camila-realm/.well-known/openid-configuration)

## 🌐 Usage

---

### Demonstration with Oauth2/OpenID

```bash
## Request authentication (in a browser)
xdg-open "http://keycloak:9191/realms/camila-realm/protocol/openid-connect/auth?response_type=code&client_id=camila-client&scope=openid&state=randomstring&redirect_uri=http://keycloak:9191/callback"
```

```bash
# The response includes a "code" to be used to request the token (copy to `random_auth_code`). 
# (This code is valid for a short period only)
client_id="camila-client"
client_secret="Fuvf8XyBDXxU57NAOOFZVvdUIPmGgiyE"
random_auth_code="bdca368b-dd52-4914-896d-efe782b07154.f78d7eb1-0485-4807-ac8d-fbe90f7cfdc9.eafde08a-ad4f-416b-835f-c3c616960ea8"

# Request the token (CLI)
access_token="$(curl --location 'http://keycloak:9191/realms/camila-realm/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=authorization_code' \
--data-urlencode "client_id=${client_id}" \
--data-urlencode "client_secret=${client_secret}" \
--data-urlencode "code=${random_auth_code}" \
--data-urlencode 'redirect_uri=http://keycloak:9191/callback' \
--data-urlencode 'scope=openid' | jq -r '.access_token')"
echo "access_token: ${access_token}"

# Request the protected resource with the received token (CLI)
curl --location 'http://gateway:8090/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
--header 'Accept: application/x-ndjson' \
--header "Authorization: Bearer ${access_token}"
```

### Using `postman`

[Readme.md](./../../../../.docs/api/Readme.md)

## 📝 Notes

---

> To export the manually configured `camila-realm` in `keycloak`, the CLI is used because not all data is exported from the GUI

> With the `keycloak` service running, configure the realm and then export it

```bash
docker exec -it keycloak bash -c "/opt/keycloak/bin/kc.sh export --dir /opt/keycloak/ --realm camila-realm --users realm_file"
docker cp keycloak:/opt/keycloak/camila-realm-realm.json camila-realm-realm.json
```
