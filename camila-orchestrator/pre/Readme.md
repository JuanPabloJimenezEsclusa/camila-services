# camila-product-orchestrator-pre

Basado en `AWS Provider`

## Pre-condiciones

* Docker >= 24.0.6
* AWS CLI >= 2.15.52

## Operaciones

| Archivo                                             | Descripción                                                                                                                              |
|-----------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| [init-aws-stack](/init-aws-stack.sh)                | Contiene un script para cargar las imágenes al registro (ECR) y crear la infraestructura a partir de una plantilla de AWS CloudFormation |
| [delete-aws-stack.sh](/delete-aws-stack.sh)         | Contiene un script para eliminar la infraestructura utilizando AWS CLI                                                                   |
| [camila-services-stack](/camila-services-stack.yml) | Plantilla con ejemplo de infraestructura de despliegue de contenedores en AWS                                                            |
| [api-requests](/api-requests.http)                  | Archivo de pruebas de peticiones al API (REST, Graphql, Websocket y RSocket)                                                             |


<p style="text-align: center">
  <img src="aws/images/template-designer-camila-product-stack.png" alt="template-designer">
  <img src="aws/images/application-composer-camila-product-stack.png" alt="application-composer">
</p>

❗ Esta infraestructura conlleva gastos. Evitar mantenerla encendida si no se está utilizando

```bash
# Init infrastructure
export COUCHBASE_CONNECTION="couchbases://cb.****.cloud.couchbase.com"
export COUCHBASE_USERNAME="juanpablo****"
export COUCHBASE_PASSWORD="*************"
export MONGO_URI="mongodb+srv://****:****@****.****.mongodb.net/camila-db?ssl=true&retryWrites=true&w=majority&maxPoolSize=200&connectTimeoutMS=5000&socketTimeoutMS=120000"

./init-aws-stack.sh
```

```bash
# Delete infrastructure

./delete-aws-stack.sh
```

```bash
# Local image test

docker run --rm -it \
  --name="camila-product-api" \
  --network=host \
  --env SPRING_PROFILES_ACTIVE=PRE \
  --env LANG=en_US.utf8 \
  --env LANGUAGE=en_US.utf8 \
  --env LC_ALL=en_US.utf8 \
  --env spring.data.mongodb.uri="mongodb+srv://****:****@****.****.mongodb.net/camila-db" \
  --env spring.data.mongodb.ssl.enabled="false" \
  --env spring.couchbase.connection-string="couchbases://cb.****.cloud.couchbase.com" \
  --env spring.couchbase.username="juanpablo****" \
  --env spring.couchbase.password="*************" \
  --env spring.couchbase.env.ssl.enabled=true \
  --env spring.application.repository.technology="mongo" \
  --memory="1024m" --memory-reservation="1024m" --memory-swap="1024m" --cpu-shares=500 \
  546053716955.dkr.ecr.eu-west-1.amazonaws.com/camila-product-api:1.0.0
```

![camila-product-api-pre-aws-example.gif](aws/images/camila-product-api-pre-aws-example.gif)

## Enlaces

* API
  * [API Rest (Swagger-ui)](https://poc.jpje-kops.xyz/product/api/webjars/swagger-ui/index.html#/)

* AWS UI
  * [AWS CloudFormation](https://eu-west-1.console.aws.amazon.com/cloudformation/home?region=eu-west-1#/stacks?filteringText=&filteringStatus=active&viewNested=true)
  * [AWS ECS Cluster](https://eu-west-1.console.aws.amazon.com/ecs/v2/clusters/camila-product-cluster/services/camila-product-service/health?region=eu-west-1) 💰
  * [AWS Certificate Manager (ACM)](https://eu-west-1.console.aws.amazon.com/acm/home?region=eu-west-1#/certificates/list)
  * [AWS VPC](https://eu-west-1.console.aws.amazon.com/vpcconsole/home?region=eu-west-1#vpcs:)
  * [AWS ECR](https://eu-west-1.console.aws.amazon.com/ecr/repositories/private/546053716955/camila-product-api?region=eu-west-1)
  * [AWS EC2 Load Balancer](https://eu-west-1.console.aws.amazon.com/ec2/home?region=eu-west-1#LoadBalancers:) 💰
  * [AWS CloudWatch](https://eu-west-1.console.aws.amazon.com/cloudwatch/home?region=eu-west-1#logsV2:log-groups)
  * [AWS Secret Manager](https://eu-west-1.console.aws.amazon.com/secretsmanager/listsecrets?region=eu-west-1) 💰

* Databases
  * [Mongo Atlas](https://cloud.mongodb.com/v2/665f45371f34d90e0237aca0#/overview)
  * [Couchbase Capella](https://cloud.couchbase.com/databases?oid=6436d8a0-3909-4aea-8ff7-1673510b6c11) 💰 (only 30 days free tier)
