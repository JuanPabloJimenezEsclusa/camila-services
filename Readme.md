# camila-services

Proyecto de servicios con |Java|Spring|

## Contenido

| Módulo                                      | Descripción                |
|---------------------------------------------|----------------------------|
| [camila-product-api](/camila-product-api)   | Contiene el API Rest que expone la consulta de productos |
| [camila-discovery](/camila-discovery)       | Contiene un descubridor de servicios |
| [camila-gateway](/camila-gateway)           | Contiene un gateway para servicios |
| [camila-config](/camila-config)             | Contiene un configurador central de servicios |
| [camila-admin](/camila-admin)               | Contiene un administrador de servicios (UI) |
| [camila-orchestrator](/camila-orchestrator) | Contiene la configuración como código para orquestar el despliegue del proyecto |

## Diagrama de arquitectura

![Arquitectura-C1](.docs/architecture/camila-service-da-v1-C1.svg "Diagrama C1")

![Arquitectura-C2](.docs/architecture/camila-service-da-v1-C2.svg "Diagrama C2")

## Domain Storytelling

En **camila.shopping**, se ha reconocido la necesidad de mejorar la organización y presentación de productos. Para abordar este desafío, se ha propuesto el desarrollo de un algoritmo de clasificación que optimice la experiencia del usuario al buscar dichos productos.

![domain-storytelling](.docs/architecture/camila-shopping-domain-v1.dst.svg "Diagrama WDS")

---

**El Problema:**
La diversidad de productos en **camila.shopping** es impresionante, pero la manera en que se presentan puede mejorarse. La tarea es organizar estos productos de manera efectiva dentro de sus respectivas categorías para facilitar la búsqueda de los clientes.

**La Solución:**
La solución propuesta es un algoritmo de clasificación que emplea una combinación ponderada de métricas, en este caso: el número de unidades vendidas y el ratio de stock. La ponderación asignará por ejemplo, un 80% de peso al número de unidades vendidas. El 20% restante se asignará al ratio de stock, siendo estas ponderaciones variables en cada consulta.

**La Presentación de Resultados:**
La información organizada será expuesta a través de una API REST. Esta interfaz proporcionará un medio eficiente para acceder a la lista de productos ordenada según las métricas definidas.

**La Implementación:**
El algoritmo estará integrado en la infraestructura de **camila.shopping** y se comunicará con los sistemas existentes para recopilar datos de ventas y stock. La información clasificada estará disponible para consumo a través de la API REST, proporcionando a los usuarios un acceso fácil y directo a los productos organizados.

**El Impacto Esperado:**
Con esta mejora, se espera optimizar la experiencia del usuario, aumentar la eficiencia en la búsqueda de productos y, elevar la satisfacción de los clientes en **camila.shopping**. La implementación del algoritmo de clasificación es un paso estratégico para mantenerse a la vanguardia en el competitivo mundo del comercio electrónico.

---

Métricas:

| name                 | description                 |
|----------------------|-----------------------------|
| Ventas por unidades  | número de unidades vendidas | 
| Ratio de stock       | ratio de tallas con stock   |

---

Muestra de productos:

| id | name                          | sales_units | stock                |
|----|-------------------------------|-------------|----------------------|
| 1  | V-NECH BASIC SHIRT            | 100         | S: 4 / M:9 / L:0     |
| 2  | CONTRASTING FABRIC T-SHIRT    | 50          | S: 35 / M:9 / L:9    |
| 3  | RAISED PRINT T-SHIRT          | 80          | S: 20 / M:2 / L:20   |
| 4  | PLEATED T-SHIRT               | 3           | S: 25 / M:30 / L:10  |
| 5  | CONTRASTING LACE T-SHIRT      | 650         | S: 0 / M:1 / L:0     |
| 6  | SLOGAN T-SHIRT                | 20          | S: 9 / M:2 / L:5     |
