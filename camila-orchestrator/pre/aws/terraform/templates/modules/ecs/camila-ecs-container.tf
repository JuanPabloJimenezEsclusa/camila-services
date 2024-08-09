
# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/secretsmanager_secret
resource "aws_secretsmanager_secret" "couchbase_password" {
  name                           = "CouchbasePassword"
  description                    = "Couchbase password"
  recovery_window_in_days        = 0
  force_overwrite_replica_secret = true

  tags = {
    Name   = "CouchbasePassword"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/secretsmanager_secret_version
resource "aws_secretsmanager_secret_version" "couchbase_password_version" {
  secret_id     = aws_secretsmanager_secret.couchbase_password.id
  secret_string = jsonencode({ password = var.couchbase_password })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/secretsmanager_secret
resource "aws_secretsmanager_secret" "mongo_uri" {
  name                           = "MongoUri"
  description                    = "Mongo URI"
  recovery_window_in_days        = 0
  force_overwrite_replica_secret = true

  tags = {
    Name   = "MongoUri"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/secretsmanager_secret_version
resource "aws_secretsmanager_secret_version" "mongo_uri_version" {
  secret_id     = aws_secretsmanager_secret.mongo_uri.id
  secret_string = jsonencode({ uri = var.mongo_uri })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/vpc
resource "aws_vpc" "main" {
  cidr_block           = "10.10.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name   = "camila-product-vpc"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/subnet
resource "aws_subnet" "public_a" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.10.1.0/24"
  map_public_ip_on_launch = true
  availability_zone       = data.aws_availability_zones.available.names[0]

  tags = {
    Name   = "camila-product-public-subnet-a"
    ENTORN = "PRE"
  }
}

resource "aws_subnet" "public_b" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.10.2.0/24"
  map_public_ip_on_launch = true
  availability_zone       = data.aws_availability_zones.available.names[1]

  tags = {
    Name   = "camila-product-public-subnet-b"
    ENTORN = "PRE"
  }
}

resource "aws_subnet" "private_a" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.10.16.0/20"
  map_public_ip_on_launch = false
  availability_zone       = data.aws_availability_zones.available.names[0]

  tags = {
    Name   = "camila-product-private-subnet-a"
    ENTORN = "PRE"
  }
}

resource "aws_subnet" "private_b" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.10.32.0/20"
  map_public_ip_on_launch = false
  availability_zone       = data.aws_availability_zones.available.names[1]

  tags = {
    Name   = "camila-product-private-subnet-b"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/internet_gateway
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name   = "camila-product-igw"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route_table
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name   = "camila-product-public-route-table"
    ENTORN = "PRE"
  }
}

# It's necessary to communicate with databases
# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/eip
resource "aws_eip" "nat" {
  domain     = "vpc"
  depends_on = [aws_internet_gateway.main]

  tags = {
    Name   = "nat-eip"
    ENTORN = "PRE"
  }
}

# It's necessary to communicate with databases
# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/nat_gateway
resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public_a.id

  tags = {
    Name   = "main-nat-gateway"
    ENTORN = "PRE"
  }
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main.id
  }

  tags = {
    Name   = "camila-product-private-route-table"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route_table_association
resource "aws_route_table_association" "public_a" {
  subnet_id      = aws_subnet.public_a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_b" {
  subnet_id      = aws_subnet.public_b.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "private_a" {
  subnet_id      = aws_subnet.private_a.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "private_b" {
  subnet_id      = aws_subnet.private_b.id
  route_table_id = aws_route_table.private.id
}

# it's necessary to see the image into the ECR private repository
# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/vpc_endpoint
resource "aws_vpc_endpoint" "ecr_api" {
  vpc_id             = aws_vpc.main.id
  service_name       = "com.amazonaws.${data.aws_region.current.name}.ecr.api"
  vpc_endpoint_type  = "Interface"
  subnet_ids         = [aws_subnet.private_a.id, aws_subnet.private_b.id]
  security_group_ids = [aws_security_group.ecs_service_sg.id, aws_security_group.lb_sg.id]

  tags = {
    Name   = "ecr-api-endpoint"
    ENTORN = "PRE"
  }
}

# it's necessary to see the image into the ECR private repository
resource "aws_vpc_endpoint" "ecr_dkr" {
  vpc_id             = aws_vpc.main.id
  service_name       = "com.amazonaws.${data.aws_region.current.name}.ecr.dkr"
  vpc_endpoint_type  = "Interface"
  subnet_ids         = [aws_subnet.private_a.id, aws_subnet.private_b.id]
  security_group_ids = [aws_security_group.ecs_service_sg.id, aws_security_group.lb_sg.id]

  tags = {
    Name   = "ecr-dkr-endpoint"
    ENTORN = "PRE"
  }
}

# Add VPC logs
# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_log_group
resource "aws_cloudwatch_log_group" "vpc_log_group" {
  name              = "/vpc/camila-product-vpc"
  retention_in_days = 1

  tags = {
    "ENTORN" = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy
resource "aws_iam_policy" "vpc_flow_log_policy" {
  name        = "vpc-flow-log-policy"
  description = "Policy for VPC Flow Logs"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogGroups",
          "logs:DescribeLogStreams"
        ]
        Resource = "arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:log-group:/vpc/camila-product-vpc:*"
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role
resource "aws_iam_role" "vpc_flow_log_role" {
  name = "vpc-flow-log-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "vpc-flow-logs.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "vpc_flow_log_policy_attachment" {
  role       = aws_iam_role.vpc_flow_log_role.name
  policy_arn = aws_iam_policy.vpc_flow_log_policy.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/flow_log
resource "aws_flow_log" "vpc_flow_log" {
  log_destination      = aws_cloudwatch_log_group.vpc_log_group.arn
  log_destination_type = "cloud-watch-logs"
  traffic_type         = "ALL"
  iam_role_arn         = aws_iam_role.vpc_flow_log_role.arn
  vpc_id               = aws_vpc.main.id

  tags = {
    "ENTORN" = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecs_cluster
resource "aws_ecs_cluster" "main" {
  name = "camila-product-cluster"

  configuration {
    execute_command_configuration {
      logging = "DEFAULT"
    }
  }

  tags = {
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecs_cluster_capacity_providers
resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name       = aws_ecs_cluster.main.name
  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    base              = 0
    weight            = 1
    capacity_provider = "FARGATE_SPOT"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_log_group
resource "aws_cloudwatch_log_group" "ecs_log_group" {
  name              = "/ecs/camila-product-backend"
  retention_in_days = 1

  tags = {
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecs_task_definition
resource "aws_ecs_task_definition" "main" {
  family                   = "camila-product-definition"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.task_execution_role.arn

  container_definitions = jsonencode([{
    name      = "camila-product-backend"
    image     = var.image_name
    cpu       = 512
    memory    = 1024
    essential = true
    linuxParameters = {
      initProcessEnabled : true
    }
    portMappings = [
      {
        name          = "http-8080"
        containerPort = 8080
        hostPort      = 8080
        protocol      = "tcp"
        appProtocol   = "http"
      },
      {
        name          = "rsocket-7000"
        containerPort = 7000
        hostPort      = 7000
        protocol      = "tcp"
        appProtocol   = "http"
      },
      {
        name          = "grpc-6565"
        containerPort = 6565
        hostPort      = 6565
        protocol      = "tcp"
        appProtocol   = "grpc"
      }
    ]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        awslogs-group         = aws_cloudwatch_log_group.ecs_log_group.id
        awslogs-region        = data.aws_region.current.name
        awslogs-stream-prefix = "ecs"
      }
    }
    secrets = [
      { name = "spring.couchbase.password", valueFrom = "${aws_secretsmanager_secret.couchbase_password.arn}:password::" },
      { name = "spring.data.mongodb.uri", valueFrom = "${aws_secretsmanager_secret.mongo_uri.arn}:uri::" }
    ]
    environment = [
      { name = "LANGUAGE", value = "en_US.utf8" },
      { name = "LANG", value = "en_US.utf8" },
      { name = "LC_ALL", value = "en_US.utf8" },
      { name = "TIME_ZONE", value = "UTC" },
      { name = "JVM_OPTIONS", value = "-Xms512m -Xmx1024m" },
      { name = "SPRING_PROFILES_ACTIVE", value = "pre" },
      { name = "SERVER_URL", value = "https://${var.domain_name}" },
      { name = "SECURITY_ISSUER_URI", value = "https://cognito-idp.${data.aws_region.current.name}.amazonaws.com/${var.user_pool_id}" },
      { name = "SECURITY_DOMAIN_URI", value = "https://camila-realm.auth.${data.aws_region.current.name}.amazoncognito.com" },
      { name = "spring.application.repository.technology", value = var.repository_technology },
      { name = "spring.couchbase.connection-string", value = var.couchbase_connection },
      { name = "spring.couchbase.username", value = var.couchbase_username },
      { name = "spring.couchbase.env.ssl.enabled", value = "true" },
      { name = "spring.data.mongodb.ssl.enabled", value = "true" },
      { name = "spring.rsocket.server.port", value = "7000" },
      { name = "grpc.server.port", value = "6565" }
    ]
  }])

  tags = {
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role
resource "aws_iam_role" "task_execution_role" {
  name        = "task-execution-role"
  description = "IAM Role for ECS Task Execution"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy
resource "aws_iam_policy" "ecs_task_execution_policy" {
  name = "ecsTaskExecutionPolicy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecs:ExecuteCommand",
          "ecs:DescribeTasks",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:GetAuthorizationToken",
          "logs:CreateLogStream",
          "logs:DescribeLogStreams",
          "logs:PutLogEvents",
          "cloudwatch:PutMetricData",
          "secretsmanager:GetSecretValue"
        ]
        Resource = "*"
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "ecs_task_execution_policy_attachment" {
  role       = aws_iam_role.task_execution_role.name
  policy_arn = aws_iam_policy.ecs_task_execution_policy.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecs_service
resource "aws_ecs_service" "camila_product_backend" {
  name                              = "camila-product-backend-service"
  cluster                           = aws_ecs_cluster.main.id
  task_definition                   = aws_ecs_task_definition.main.arn
  desired_count                     = 1
  launch_type                       = "FARGATE"
  health_check_grace_period_seconds = 30

  network_configuration {
    subnets          = [aws_subnet.private_a.id, aws_subnet.private_b.id]
    security_groups  = [aws_security_group.ecs_service_sg.id]
    # ECS service and its containers won't be available to internet
    assign_public_ip = false
  }

  load_balancer {
    container_name   = "camila-product-backend"
    container_port   = 8080
    target_group_arn = aws_lb_target_group.web_target_group.arn
  }
  load_balancer {
    container_name   = "camila-product-backend"
    container_port   = 7000
    target_group_arn = aws_lb_target_group.rsocket_target_group.arn
  }
  load_balancer {
    container_name   = "camila-product-backend"
    container_port   = 6565
    target_group_arn = aws_lb_target_group.grpc_target_group.arn
  }

  tags = {
    ENTORN = "PRE"
  }

  depends_on = [
    aws_lb_listener.http,
    aws_lb_listener.rsocket,
    aws_lb_listener.grpc
  ]

  lifecycle {
    ignore_changes = [desired_count]
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/security_group
resource "aws_security_group" "lb_sg" {
  name        = "camila-product-load-balancer-sg"
  description = "Security group for load balancer"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    description = "Allow 80 HTTP traffic"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    description = "Allow 443 HTTPS traffic"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 7001
    to_port     = 7001
    protocol    = "tcp"
    description = "Allow 7001 RSocket traffic"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 50051
    to_port     = 50051
    protocol    = "tcp"
    description = "Allow 50051 GRPC traffic"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name   = "camila-product-lb-sg"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/security_group
resource "aws_security_group" "ecs_service_sg" {
  name        = "camila-product-backend-sg"
  description = "Security group for camila-product backend service"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.lb_sg.id]
    description     = "Allow http traffic from the load balancer"
  }
  ingress {
    from_port       = 7000
    to_port         = 7000
    protocol        = "tcp"
    security_groups = [aws_security_group.lb_sg.id]
    description     = "Allow rsocket traffic from the load balancer"
  }
  ingress {
    from_port       = 6565
    to_port         = 6565
    protocol        = "tcp"
    security_groups = [aws_security_group.lb_sg.id]
    description     = "Allow grpc traffic from the load balancer"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name   = "camila-product-ecs-sg"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lb
resource "aws_lb" "main" {
  name                       = "camila-product-lb"
  internal                   = false
  load_balancer_type         = "application"
  security_groups            = [aws_security_group.lb_sg.id]
  subnets                    = [aws_subnet.public_a.id, aws_subnet.public_b.id]
  enable_deletion_protection = "false"

  tags = {
    Name   = "camila-product-lb"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lb_target_group
resource "aws_lb_target_group" "web_target_group" {
  name        = "web-target-group"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"
  health_check {
    path                = "/product/api/actuator/health/ping"
    port                = 8080
    protocol            = "HTTP"
    interval            = 60
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }

  tags = {
    Name   = "camila-product-tg"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lb_target_group
resource "aws_lb_target_group" "rsocket_target_group" {
  name        = "rsocket-target-group"
  port        = 7000
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"
  health_check {
    path                = "/product/api/actuator/health/ping"
    port                = 8080
    protocol            = "HTTP"
    interval            = 60
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }

  tags = {
    Name   = "camila-product-tg"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lb_target_group
resource "aws_lb_target_group" "grpc_target_group" {
  name             = "grpc-target-group"
  port             = 6565
  protocol         = "HTTP"
  protocol_version = "GRPC" # This is a key to make it work!
  vpc_id           = aws_vpc.main.id
  target_type      = "ip"
  health_check {
    path                = "/"
    port                = 6565
    protocol            = "HTTP"
    interval            = 60
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
    matcher             = "0-20"
  }

  tags = {
    Name   = "camila-product-tg"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate
resource "aws_acm_certificate" "main" {
  domain_name       = var.domain_name
  validation_method = "DNS"

  options {
    certificate_transparency_logging_preference = "DISABLED"
  }

  tags = {
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.main.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      type   = dvo.resource_record_type
      record = dvo.resource_record_value
    }
  }

  zone_id = var.hosted_zone_id
  name    = each.value.name
  type    = each.value.type
  records = [each.value.record]
  ttl     = 60
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate_validation
resource "aws_acm_certificate_validation" "cert_validation" {
  certificate_arn = aws_acm_certificate.main.arn
  validation_record_fqdns = [
    for record in aws_route53_record.cert_validation : record.fqdn
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lb_listener
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"
    redirect {
      port        = 443
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }

  tags = {
    Name   = "camila-product-lb-http-listener"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lb_listener
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.main.arn
  port              = 443
  protocol          = "HTTPS"
  certificate_arn   = aws_acm_certificate.main.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.web_target_group.arn
  }

  tags = {
    Name   = "camila-product-lb-https-listener"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lb_listener_certificate
resource "aws_lb_listener_certificate" "https_listener_certificate" {
  listener_arn    = aws_lb_listener.https.arn
  certificate_arn = aws_acm_certificate.main.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lb_listener
resource "aws_lb_listener" "rsocket" {
  load_balancer_arn = aws_lb.main.arn
  port              = 7001
  protocol          = "HTTPS"
  certificate_arn   = aws_acm_certificate.main.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.rsocket_target_group.arn
  }

  tags = {
    Name   = "camila-product-lb-rsocket-listener"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lb_listener_certificate
resource "aws_lb_listener_certificate" "rsocket_listener_certificate" {
  listener_arn    = aws_lb_listener.rsocket.arn
  certificate_arn = aws_acm_certificate.main.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lb_listener
resource "aws_lb_listener" "grpc" {
  load_balancer_arn = aws_lb.main.arn
  port              = 50051
  protocol          = "HTTPS"
  certificate_arn   = aws_acm_certificate.main.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.grpc_target_group.arn
  }

  tags = {
    Name   = "camila-product-lb-grpc-listener"
    ENTORN = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lb_listener_certificate
resource "aws_lb_listener_certificate" "grpc_listener_certificate" {
  listener_arn    = aws_lb_listener.grpc.arn
  certificate_arn = aws_acm_certificate.main.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "main" {
  zone_id = var.hosted_zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = aws_lb.main.dns_name
    zone_id                = aws_lb.main.zone_id
    evaluate_target_health = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role
resource "aws_iam_role" "task_autoscaling_role" {
  name        = "task_autoscaling_role"
  description = "IAM Role for AutoScaling Task Execution"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "application-autoscaling.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy
resource "aws_iam_policy" "task_autoscaling_role_policy" {
  name = "camila-product-scaling-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "application-autoscaling:*",
          "cloudwatch:ListMetrics",
          "cloudwatch:DescribeAlarms",
          "cloudwatch:GetMetricStatistics",
          "cloudwatch:SetAlarmState",
          "cloudwatch:DeleteAlarms",
          "cloudwatch:PutMetricAlarm",
          "cloudwatch:PutMetricData",
          "ecs:CreateService",
          "ecs:UpdateService",
          "ecs:DescribeServices",
          "ecs:DescribeClusters"
        ]
        Resource = "*"
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "task_autoscaling_role_policy_attachment" {
  role       = aws_iam_role.task_autoscaling_role.name
  policy_arn = aws_iam_policy.task_autoscaling_role_policy.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/appautoscaling_target
resource "aws_appautoscaling_target" "ecs_service" {
  max_capacity       = 2
  min_capacity       = 1
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.camila_product_backend.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
  role_arn           = aws_iam_role.task_autoscaling_role.arn

  lifecycle {
    ignore_changes = [role_arn]
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/appautoscaling_policy
resource "aws_appautoscaling_policy" "scale_up" {
  name               = "scale-up"
  resource_id        = aws_appautoscaling_target.ecs_service.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_service.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_service.service_namespace
  policy_type        = "StepScaling"

  step_scaling_policy_configuration {
    adjustment_type         = "ChangeInCapacity"
    cooldown                = 300
    metric_aggregation_type = "Average"
    step_adjustment {
      metric_interval_lower_bound = 0
      scaling_adjustment          = 1
    }
  }
}

resource "aws_appautoscaling_policy" "scale_down" {
  name               = "scale-down"
  resource_id        = aws_appautoscaling_target.ecs_service.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_service.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_service.service_namespace
  policy_type        = "StepScaling"

  step_scaling_policy_configuration {
    adjustment_type         = "ChangeInCapacity"
    cooldown                = 300
    metric_aggregation_type = "Average"
    step_adjustment {
      metric_interval_lower_bound = 0
      scaling_adjustment          = -1
    }
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_metric_alarm
resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  alarm_name          = "camila-product-cpu-alarm-high"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "1"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = "60"
  statistic           = "Average"
  threshold           = "50"
  alarm_description   = "Alarm if CPU exceeds 50%"
  actions_enabled     = "true"
  dimensions = {
    ClusterName = aws_ecs_cluster.main.name
    ServiceName = aws_ecs_service.camila_product_backend.name
  }
  alarm_actions = [aws_appautoscaling_policy.scale_up.arn]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_metric_alarm
resource "aws_cloudwatch_metric_alarm" "cpu_low" {
  alarm_name          = "camila-product-cpu-alarm-low"
  comparison_operator = "LessThanOrEqualToThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = "180"
  statistic           = "Average"
  threshold           = "10"
  alarm_description   = "Alarm if CPU is below 10%"
  actions_enabled     = "true"
  dimensions = {
    ClusterName = aws_ecs_cluster.main.name
    ServiceName = aws_ecs_service.camila_product_backend.name
  }
  alarm_actions = [aws_appautoscaling_policy.scale_down.arn]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_metric_alarm
resource "aws_cloudwatch_metric_alarm" "request_count" {
  alarm_name          = "camila-product-elb-request-count-alarm"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "1"
  datapoints_to_alarm = "1"
  metric_name         = "RequestCount"
  namespace           = "AWS/ApplicationELB"
  period              = "30"
  statistic           = "Sum"
  threshold           = "1"
  alarm_description   = "Alarm when request count exceeds 0"
  actions_enabled     = "true"
  treat_missing_data  = "missing"
  dimensions = {
    LoadBalancer = aws_lb.main.id
  }
  alarm_actions = [aws_appautoscaling_policy.scale_up.arn]
}
