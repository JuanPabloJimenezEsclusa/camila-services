# camila-orchestration dev authorization

Para configurar `keycloak` se importa el archivo: [camila-realm.json](./camila-realm.json) el cual contiene:

* **realm**: `camila-realm`
* **scopes**: `camila.read`, `camila.write`
* **client**: (id) `camila-client` (secret) `Fuvf8XyBDXxU57NAOOFZVvdUIPmGgiyE`
* **user**: `camila/camila`

> En esta configuración se utilizan los hostnames `keycloak` y `gateway` que deberían estar en el archivo: `/etc/hosts`

## Enlaces

* [oauth playground](https://www.oauth.com/playground)
* [local keycloak openid-configuration](http://keycloak:9191/realms/camila-realm/.well-known/openid-configuration)

## Demostración con Oauth2/OpenID

```bash
## solicitar la autenticación (en un navegador)
http://keycloak:9191/realms/camila-realm/protocol/openid-connect/auth?response_type=code&client_id=camila-client&scope=openid&state=randomstring&redirect_uri=http://keycloak:9191/callback

# la respuesta incluye un "code" a utilizar para solicitar el token (copiar en `random_auth_code`) (este código esta vigente por poco tiempo)
client_id="camila-client"
client_secret="Fuvf8XyBDXxU57NAOOFZVvdUIPmGgiyE"
random_auth_code="a1bb7a3d-501e-413a-9789-278daad56435.d2d8dcd5-515a-4744-8d1d-a572adcc2119.eafde08a-ad4f-416b-835f-c3c616960ea8"

# solicitar el token (cli)
access_token="$(curl --location 'http://keycloak:9191/realms/camila-realm/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=authorization_code' \
--data-urlencode "client_id=${client_id}" \
--data-urlencode "client_secret=${client_secret}" \
--data-urlencode "code=${random_auth_code}" \
--data-urlencode 'redirect_uri=http://keycloak:9191/callback' \
--data-urlencode 'scope=openid' | jq -r '.access_token')"
echo "access_token: ${access_token}"

# petición al recurso protegido con el token recibido (cli)
curl --location 'http://gateway:8090/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
--header 'Accept: application/json' \
--header "Authorization: Bearer ${access_token}"
```

## Utilizando `postman`

[Readme.md](./../../../../.docs/api/Readme.md)

## Utilidades

> Para exportar el realm: `camila-realm` configurado manualmente en `keycloak` se utiliza `cli` porque desde la GUI no se exportan todos los datos

> Con el servicio `keycloak` ejecutándose, se configura el realm y luego se exporta 

```bash
docker exec -it keycloak bash -c "/opt/keycloak/bin/kc.sh export --dir /opt/keycloak/ --realm camila-realm --users realm_file"
docker cp keycloak:/opt/keycloak/camila-realm-realm.json camila-realm.json
```
