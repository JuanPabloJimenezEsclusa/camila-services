output "api_gateway_domain_name" {
  description = "The domain name of the API Gateway"
  value       = aws_api_gateway_domain_name.camila_api_gateway_domain.domain_name
}

output "api_gateway_hosted_zone_id" {
  description = "The Route53 Hosted Zone ID that the API Gateway's domain name is hosted in"
  value       = aws_api_gateway_domain_name.camila_api_gateway_domain.cloudfront_zone_id
}

output "api_gateway_regional_domain_name" {
  description = "The regional domain name of the API Gateway"
  value       = aws_api_gateway_domain_name.camila_api_gateway_domain.regional_domain_name
}

output "api_gateway_regional_hosted_zone_id" {
  description = "The Route53 Hosted Zone ID that the API Gateway's regional domain name is hosted in"
  value       = aws_api_gateway_domain_name.camila_api_gateway_domain.regional_zone_id
}

output "api_gateway_base_path_mapping_id" {
  description = "The ID of the API Gateway Base Path Mapping"
  value       = aws_api_gateway_base_path_mapping.camila_api_gateway_base_path.id
}

output "api_gateway_route53_record_name" {
  description = "The name of the Route53 record for the API Gateway domain"
  value       = aws_route53_record.camila_api_gateway_record.name
}

output "camila_aws_api_gateway_stage_arn" {
  description = "The ARN of the API Gateway"
  value = aws_api_gateway_stage.camila_api_gateway_stage.arn
}
