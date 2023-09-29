# camila-product-api-operate

Contiene información sobre como compilar, desplegar y ejecutar el proyecto localmente

## Scripts

| Nombre     | Descripción                          |
|------------|--------------------------------------|
| [build-aot](./build-aot.sh) | Construye el proyecto con aot  |
| [build-image](./build-image.sh) | Construye el proyecto y genera una imagen |
| [build-image-native](./build-image-native.sh) | Construye el proyecto nativo y genera una imagen |
| [run-spring-boot](./run-spring-boot.sh) | Ejecuta el proyecto a traves del plugin de `spring-boot` |
| [run-aot](./run-aot.sh) | Ejecuta el proyecto con aot |
| [run-image](./run-image.sh) | Ejecuta el proyecto en contenedor |
| [run-ddbb-local](./run-ddbb-local.sh) | Construye una BBDD de prueba en contenedor |

## Notas

```sql
# Script inicial de BBDD

use camila-db
db.createUser({user: "camilauser", pwd: "camilauser", roles : [{role: "readWrite", db: "camila-db"}]})

db.products.insertMany([
    { "internalId": "1", "name": "V-NECH BASIC SHIRT", "category": "SHIRT", "salesUnits": 100, "stock": { "S": 4, "M": 9, "L": 0 }},
    { "internalId": "2", "name": "CONTRASTING FABRIC T-SHIRT", "category": "SHIRT", "salesUnits": 50, "stock": { "S": 35, "M": 9, "L": 9 }},
    { "internalId": "3", "name": "RAISED PRINT T-SHIRT", "category": "SHIRT", "salesUnits": 80,  "stock": { "S": 20, "M": 2, "L": 20 }},
    { "internalId": "4", "name": "PLEATED T-SHIRT", "category": "SHIRT", "salesUnits": 3, "stock": { "S": 25, "M": 30, "L": 10 }},
    { "internalId": "5", "name": "CONTRASTING LACE T-SHIRT", "category": "SHIRT", "salesUnits": 650, "stock": { "S": 0, "M": 1, "L": 0 }},
    { "internalId": "6", "name": "SLOGAN T-SHIRT", "category": "SHIRT", "salesUnits": 20, "stock": { "S": 9, "M": 2, "L": 5 }}
]);
```
