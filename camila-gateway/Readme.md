# camila-gateway

Contiene un gateway para servicios (incluye los patrones: `circuit-breaker` y `retry`)

## Pre-condiciones

* JDK >= 17
* Docker >= 24.0.6
* maven >= 3.9.4
* GraalVM >= 22.3.2
* Spring >= 6.x
* Spring-boot >= 3.1.x

## Arquitectura

```txt
📦gateway
 ┣ 📂presentation
 ┗ 📜CamilaGatewayApplication.java
```

## Enlaces

* API DOC (dev): <http://localhost:8090/swagger-ui.html>
* Actuator:
  * <http://localhost:8090/actuator/health>
  * <http://localhost:8090/actuator/metrics/spring.cloud.gateway.requests>

## Ejemplos de petición API

```bash
curl -X 'GET' \
  'http://localhost:8090/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  -H 'accept: application/json'
  
curl -X 'GET' \
  'http://localhost:8090/product-dev/api/products/1' \
  -H 'accept: application/json'
```

## Ejemplos con seguridad: `oauth2` delegada a un servicio: `SSO` 

> Es necesario configurar el archivo: `/etc/hosts` para enlazar: `127.0.0.1  gateway  keycloak` y configurar `keycloak` como servicio: single sing-on (SSO)

```bash
curl --location 'http://gateway:8090/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  --header 'Accept: application/json' \
  --header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJfd1NBMUMxT1prbEw1MnBZQ2E0QVVwNTR5TXJfVW4zYzM1VlEyR3JwTEpjIn0.eyJleHAiOjE2OTc4OTk0ODEsImlhdCI6MTY5Nzg5OTE4MSwiYXV0aF90aW1lIjoxNjk3ODk5MTgxLCJqdGkiOiJlY2E3OWU0MC02Y2QwLTQzMmUtYjI1OC1mMjI0YzVmOGY0MjIiLCJpc3MiOiJodHRwOi8va2V5Y2xvYWs6OTE5MS9yZWFsbXMvY2FtaWxhLXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjZjMWM4M2I1LTNlNjEtNDBlNy05NGQ2LTE3YWU3NWM4NmU5MiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImNhbWlsYS1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiOWJlMWE5YWYtMWM0Ny00ODc3LWI0YjItZDhhMDAxNWNmNTQ5IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vZ2F0ZXdheTo4MDkwIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLWNhbWlsYS1yZWFsbSIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBlbWFpbCBjYW1pbGEud3JpdGUgcHJvZmlsZSBjYW1pbGEucmVhZCIsInNpZCI6IjliZTFhOWFmLTFjNDctNDg3Ny1iNGIyLWQ4YTAwMTVjZjU0OSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoiQ2FtaWxhIFNlcnZpY2VzIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiY2FtaWxhIiwiZ2l2ZW5fbmFtZSI6IkNhbWlsYSIsImZhbWlseV9uYW1lIjoiU2VydmljZXMiLCJlbWFpbCI6Im9sYmFwbnVhakBnbWFpbC5jb20ifQ.vRMUkSSdgkzfZWaY189wvOlfFhaipBkW7Q9a602GCygksY1U91KFyT-3YN7PqFjIiQHmFn-wnIz1ubBTmUBsHrl-CFjIJY0i89EcCxlFpDAhG8tW_FIWOpUeG_JQufCUMx_8YjR1tF2_Foo6YAujLrHnJhmTo37p7FaBL_uHqhlA91tuCvCTucKDgS6t2LYKr8B2QhVUQElOcrT4b56Q6YwdkYiHBoH3ciE7O18qrarM5Lh1-ZiynaRpP5MDrM3WZGZ6pzEDFcwflZavhl6J5xXlPD-Qw63WA1ebCG1YZNe0fVwIc2ZOHzNjdmxX-UrWL4KCzbtMoNaki6otB3TXLA'

curl --location 'http://gateway:8090/product-dev/api/products/1' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJfd1NBMUMxT1prbEw1MnBZQ2E0QVVwNTR5TXJfVW4zYzM1VlEyR3JwTEpjIn0.eyJleHAiOjE2OTc4OTk0ODEsImlhdCI6MTY5Nzg5OTE4MSwiYXV0aF90aW1lIjoxNjk3ODk5MTgxLCJqdGkiOiJlY2E3OWU0MC02Y2QwLTQzMmUtYjI1OC1mMjI0YzVmOGY0MjIiLCJpc3MiOiJodHRwOi8va2V5Y2xvYWs6OTE5MS9yZWFsbXMvY2FtaWxhLXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjZjMWM4M2I1LTNlNjEtNDBlNy05NGQ2LTE3YWU3NWM4NmU5MiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImNhbWlsYS1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiOWJlMWE5YWYtMWM0Ny00ODc3LWI0YjItZDhhMDAxNWNmNTQ5IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vZ2F0ZXdheTo4MDkwIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLWNhbWlsYS1yZWFsbSIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBlbWFpbCBjYW1pbGEud3JpdGUgcHJvZmlsZSBjYW1pbGEucmVhZCIsInNpZCI6IjliZTFhOWFmLTFjNDctNDg3Ny1iNGIyLWQ4YTAwMTVjZjU0OSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoiQ2FtaWxhIFNlcnZpY2VzIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiY2FtaWxhIiwiZ2l2ZW5fbmFtZSI6IkNhbWlsYSIsImZhbWlseV9uYW1lIjoiU2VydmljZXMiLCJlbWFpbCI6Im9sYmFwbnVhakBnbWFpbC5jb20ifQ.vRMUkSSdgkzfZWaY189wvOlfFhaipBkW7Q9a602GCygksY1U91KFyT-3YN7PqFjIiQHmFn-wnIz1ubBTmUBsHrl-CFjIJY0i89EcCxlFpDAhG8tW_FIWOpUeG_JQufCUMx_8YjR1tF2_Foo6YAujLrHnJhmTo37p7FaBL_uHqhlA91tuCvCTucKDgS6t2LYKr8B2QhVUQElOcrT4b56Q6YwdkYiHBoH3ciE7O18qrarM5Lh1-ZiynaRpP5MDrM3WZGZ6pzEDFcwflZavhl6J5xXlPD-Qw63WA1ebCG1YZNe0fVwIc2ZOHzNjdmxX-UrWL4KCzbtMoNaki6otB3TXLA'
```

## Operaciones (build, deploy)

[Operar - Readme](.operate/Readme.md)
