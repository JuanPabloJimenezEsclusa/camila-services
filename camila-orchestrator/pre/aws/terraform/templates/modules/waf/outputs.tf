output "camila_gateway_acl_id" {
  description = "The ID of the WAFv2 Web ACL"
  value       = aws_wafv2_web_acl.camila_gateway_acl.id
}
