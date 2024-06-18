output "user_pool_id" {
  description = "User Pool ID"
  value       = aws_cognito_user_pool.camila_services_pool.id
}

output "user_pool_client_id" {
  description = "User Pool Client ID"
  value       = aws_cognito_user_pool_client.camila_client.id
}

output "user_pool_client_credentials_id" {
  description = "User Pool Client Credentials ID"
  value       = aws_cognito_user_pool_client.camila_client_credentials.id
}

output "user_pool_domain" {
  description = "User Pool Domain"
  value       = aws_cognito_user_pool_domain.camila_domain.domain
}

output "camila_user_id" {
  description = "Camila User ID"
  value       = aws_cognito_user.camila_user.id
}
