# camila-product-api-operate

Contiene información sobre como compilar, desplegar y ejecutar el proyecto localmente

❗ Se puede activar el perfil `chaos-monkey` para probar localmente [Principles Of Chaos](https://principlesofchaos.org/)

## Scripts

| Nombre                                                    | Descripción                                              |
|-----------------------------------------------------------|----------------------------------------------------------|
| [build-aot](./build-aot.sh)                               | Construye el proyecto con aot                            |
| [build-image](./build-image.sh)                           | Construye el proyecto y genera una imagen                |
| [build-image-native](./build-image-native.sh)             | Construye el proyecto nativo y genera una imagen         |
| [run-spring-boot](./run-spring-boot.sh)                   | Ejecuta el proyecto a traves del plugin de `spring-boot` |
| [run-aot](./run-aot.sh)                                   | Ejecuta el proyecto con aot                              |
| [run-image](./run-image.sh)                               | Ejecuta el proyecto en contenedor                        |
| [run-ddbb-mongo-local](./run-ddbb-mongo-local.sh)         | Construye una BBDD (mongo) de prueba en contenedor       |
| [init-ddbb-mongo-local](./init-ddbb-mongo-local.sh)       | Carga data de pruebas en la BBDD                         |
| [cleanup-ddbb-mongo-local](./cleanup-ddbb-mongo-local.sh) | Eliminar usuario y base de datos                         |
| [run-ddbb-couchbase-local](./run-ddbb-couchbase-local.sh) | Construye una BBDD (couchbase) de prueba en contenedor   |
