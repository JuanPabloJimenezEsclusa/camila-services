
# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/wafv2_web_acl
resource "aws_wafv2_web_acl" "camila_gateway_acl" {
  name        = "camila-gateway-acl"
  description = "WAF for Camila API Gateway"
  scope       = "REGIONAL"

  default_action {
    allow {}
  }

  rule {
    name     = "block-bad-requests"
    priority = 1

    action {
      block {}
    }

    statement {
      byte_match_statement {
        search_string         = "bad-bot"
        field_to_match {
          single_header {
            name = "user-agent"
          }
        }
        text_transformation {
          priority = 0
          type     = "NONE"
        }
        positional_constraint = "CONTAINS"
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "block-bad-requests"
      sampled_requests_enabled   = true
    }
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "api-gateway-waf"
    sampled_requests_enabled   = true
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/wafv2_web_acl_association
resource "aws_wafv2_web_acl_association" "camila_gateway_waf_association" {
  resource_arn = var.camila_api_gateway_arn
  web_acl_arn  = aws_wafv2_web_acl.camila_gateway_acl.arn
}
