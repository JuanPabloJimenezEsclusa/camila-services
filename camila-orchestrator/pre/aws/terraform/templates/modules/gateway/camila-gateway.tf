
# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role
resource "aws_iam_role" "api_gateway_cloudwatch_role" {
  name = "api-gateway-cloudwatch-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "apigateway.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    "ENTORN" = "PRE"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy
resource "aws_iam_role_policy" "api_gateway_cloudwatch_policy" {
  name = "api-gateway-cloudwatch-policy"
  role = aws_iam_role.api_gateway_cloudwatch_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:DescribeLogGroups",
          "logs:DescribeLogStreams",
          "logs:PutLogEvents",
          "logs:GetLogEvents",
          "logs:FilterLogEvents"
        ]
        Resource = "arn:aws:logs:*:*:*"
      }
    ]
  })
}

# https://registry.terraform.io/providers/figma/aws-4-49-0/latest/docs/resources/api_gateway_rest_api
resource "aws_api_gateway_rest_api" "camila_api_gateway" {
  name                         = "camila-api-gateway"
  description                  = "Camila API Gateway with ALB integration"
  put_rest_api_mode            = "merge"
  binary_media_types           = ["*/*"]
  disable_execute_api_endpoint = false

  endpoint_configuration {
    types = ["EDGE"]
  }

  tags = {
    "Name"   = "camila-api-gateway"
    "Tool"   = "terraform"
    "ENTORN" = "PRE"
  }
}

# https://registry.terraform.io/providers/aaronfeng/aws/latest/docs/resources/api_gateway_resource
resource "aws_api_gateway_resource" "proxy" {
  rest_api_id = aws_api_gateway_rest_api.camila_api_gateway.id
  parent_id   = aws_api_gateway_rest_api.camila_api_gateway.root_resource_id
  path_part   = "{proxy+}"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/api_gateway_authorizer
resource "aws_api_gateway_authorizer" "cognito" {
  name          = "cognito-authorizer"
  type          = "COGNITO_USER_POOLS"
  rest_api_id   = aws_api_gateway_rest_api.camila_api_gateway.id
  provider_arns = [var.user_pool_arn]
}

# https://registry.terraform.io/providers/aaronfeng/aws/latest/docs/resources/api_gateway_method
resource "aws_api_gateway_method" "any" {
  rest_api_id   = aws_api_gateway_rest_api.camila_api_gateway.id
  resource_id   = aws_api_gateway_resource.proxy.id
  http_method   = "ANY"
  #authorization = "NONE"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.cognito.id
  authorization_scopes = ["camila/read","camila/write"]

  request_parameters = {
    "method.request.path.proxy"           = true
    "method.request.header.Accept"        = false
    "method.request.header.Content-Type"  = false
    "method.request.header.Authorization" = false
  }
}

# https://registry.terraform.io/providers/figma/aws-4-49-0/latest/docs/resources/api_gateway_integration
resource "aws_api_gateway_integration" "alb" {
  rest_api_id             = aws_api_gateway_rest_api.camila_api_gateway.id
  resource_id             = aws_api_gateway_resource.proxy.id
  http_method             = aws_api_gateway_method.any.http_method
  integration_http_method = "ANY"
  type                    = "HTTP_PROXY"
  uri                     = "https://${var.domain_name}/product/api/{proxy}"
  timeout_milliseconds    = 15000

  request_parameters = {
    "integration.request.path.proxy"           = "method.request.path.proxy"
    "integration.request.header.Accept"        = "method.request.header.Accept"
    "integration.request.header.Content-Type"  = "method.request.header.Content-Type"
    "integration.request.header.Authorization" = "method.request.header.Authorization"
  }

  passthrough_behavior = "WHEN_NO_MATCH"
  connection_type      = "INTERNET"
}

# https://registry.terraform.io/providers/aaronfeng/aws/latest/docs/resources/api_gateway_deployment
resource "aws_api_gateway_deployment" "camila_api_gateway_deploy" {
  depends_on  = [aws_api_gateway_integration.alb]
  description = "Camila API Gateway Deployment"
  rest_api_id = aws_api_gateway_rest_api.camila_api_gateway.id

  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_resource.proxy.id,
      aws_api_gateway_method.any.id,
      aws_api_gateway_integration.alb.id,
    ]))
  }

  lifecycle {
    create_before_destroy = true
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/api_gateway_stage
resource "aws_api_gateway_stage" "camila_api_gateway_stage" {
  depends_on           = [aws_api_gateway_deployment.camila_api_gateway_deploy]
  deployment_id        = aws_api_gateway_deployment.camila_api_gateway_deploy.id
  rest_api_id          = aws_api_gateway_rest_api.camila_api_gateway.id
  stage_name           = "PRE"
  xray_tracing_enabled = false

  tags = {
    "Name"   = "camila-api-gateway"
    "Tool"   = "terraform"
    "ENTORN" = "PRE"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/api_gateway_method_settings
resource "aws_api_gateway_method_settings" "camila_api_gateway" {
  rest_api_id = aws_api_gateway_rest_api.camila_api_gateway.id
  stage_name  = aws_api_gateway_stage.camila_api_gateway_stage.stage_name
  method_path = "*/*"

  settings {
    cache_ttl_in_seconds = 60
    logging_level        = "INFO"
    data_trace_enabled   = true
    metrics_enabled      = true
    caching_enabled      = true
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/api_gateway_account
resource "aws_api_gateway_account" "camila_api_gateway" {
  cloudwatch_role_arn = aws_iam_role.api_gateway_cloudwatch_role.arn
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate
resource "aws_acm_certificate" "camila_api_gateway_certificate" {
  domain_name       = var.gateway_domain_name
  validation_method = "DNS"

  options {
    certificate_transparency_logging_preference = "DISABLED"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.camila_api_gateway_certificate.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      type   = dvo.resource_record_type
      record = dvo.resource_record_value
    }
  }

  zone_id = var.gateway_hosted_zone_id
  name    = each.value.name
  type    = each.value.type
  records = [each.value.record]
  ttl     = 60
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate_validation
resource "aws_acm_certificate_validation" "cert_validation" {
  certificate_arn = aws_acm_certificate.camila_api_gateway_certificate.arn
  validation_record_fqdns = [
    for record in aws_route53_record.cert_validation : record.fqdn
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/api_gateway_domain_name
resource "aws_api_gateway_domain_name" "camila_api_gateway_domain" {
  depends_on               = [aws_acm_certificate_validation.cert_validation]
  domain_name              = aws_acm_certificate.camila_api_gateway_certificate.domain_name
  regional_certificate_arn = aws_acm_certificate.camila_api_gateway_certificate.arn
  security_policy          = "TLS_1_2"

  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/api_gateway_base_path_mapping
resource "aws_api_gateway_base_path_mapping" "camila_api_gateway_base_path" {
  api_id      = aws_api_gateway_rest_api.camila_api_gateway.id
  stage_name  = aws_api_gateway_stage.camila_api_gateway_stage.stage_name
  domain_name = aws_api_gateway_domain_name.camila_api_gateway_domain.domain_name
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record
resource "aws_route53_record" "camila_api_gateway_record" {
  name    = var.gateway_domain_name
  type    = "A"
  zone_id = var.gateway_hosted_zone_id

  alias {
    evaluate_target_health = false
    name                   = aws_api_gateway_domain_name.camila_api_gateway_domain.regional_domain_name
    zone_id                = aws_api_gateway_domain_name.camila_api_gateway_domain.regional_zone_id
  }
}
