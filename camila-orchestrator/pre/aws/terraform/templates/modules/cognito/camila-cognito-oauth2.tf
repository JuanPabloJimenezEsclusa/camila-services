
# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cognito_user_pool
resource "aws_cognito_user_pool" "camila_services_pool" {
  name                     = var.camila-realm
  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  schema {
    attribute_data_type = "String"
    name                = "email"
    required            = true
    mutable             = true
  }

  password_policy {
    minimum_length                   = 6
    require_lowercase                = false
    require_uppercase                = false
    require_numbers                  = false
    require_symbols                  = false
    temporary_password_validity_days = 10
  }

  admin_create_user_config {
    allow_admin_create_user_only = false
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_log_group
resource "aws_cloudwatch_log_group" "camila_log_group" {
  name              = var.camila_log_group
  retention_in_days = 1
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cognito_resource_server
resource "aws_cognito_resource_server" "main" {
  identifier   = "camila"
  name         = "camila-resource-server"
  user_pool_id = aws_cognito_user_pool.camila_services_pool.id
  scope {
    scope_description = "Read Camila Services access"
    scope_name        = "read"
  }
  scope {
    scope_description = "Write Camila Services access"
    scope_name        = "write"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cognito_user_pool_client
resource "aws_cognito_user_pool_client" "camila_client" {
  name                                          = "camila-client"
  user_pool_id                                  = aws_cognito_user_pool.camila_services_pool.id
  generate_secret                               = true
  allowed_oauth_flows                           = ["code", "implicit"]
  allowed_oauth_flows_user_pool_client          = true
  prevent_user_existence_errors                 = "ENABLED"
  enable_token_revocation                       = true
  enable_propagate_additional_user_context_data = true
  supported_identity_providers                  = ["COGNITO"]

  explicit_auth_flows = [
    "ADMIN_NO_SRP_AUTH",
    "CUSTOM_AUTH_FLOW_ONLY",
    "USER_PASSWORD_AUTH"
    # "ALLOW_REFRESH_TOKEN_AUTH", "ALLOW_USER_PASSWORD_AUTH", "ALLOW_USER_SRP_AUTH"
    #  are not supported by terraform
  ]

  allowed_oauth_scopes = [
    "email",
    "openid",
    "profile",
    "camila/read",
    "camila/write"
  ]

  callback_urls = [
    "https://poc.jpje-kops.xyz/callback",
    "https://oauth.pstmn.io/v1/callback"
  ]

  logout_urls = [
    "https://camila-realm.auth.eu-west-1.amazoncognito.com/logout"
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cognito_user_pool_client
resource "aws_cognito_user_pool_client" "camila_client_credentials" {
  name                                          = "camila-client-credentials"
  user_pool_id                                  = aws_cognito_user_pool.camila_services_pool.id
  generate_secret                               = true
  allowed_oauth_flows                           = ["client_credentials"]
  supported_identity_providers                  = ["COGNITO"]
  allowed_oauth_flows_user_pool_client          = true
  prevent_user_existence_errors                 = "ENABLED"
  enable_token_revocation                       = true
  enable_propagate_additional_user_context_data = true

  explicit_auth_flows = [
    "ADMIN_NO_SRP_AUTH",
    "CUSTOM_AUTH_FLOW_ONLY",
    "USER_PASSWORD_AUTH"
    # "ALLOW_REFRESH_TOKEN_AUTH", "ALLOW_USER_PASSWORD_AUTH", "ALLOW_USER_SRP_AUTH"
    #  are not supported by terraform
  ]

  allowed_oauth_scopes = [
    "camila/read",
    "camila/write"
  ]

  callback_urls = [
    "https://poc.jpje-kops.xyz/callback",
    "https://oauth.pstmn.io/v1/callback"
  ]

  logout_urls = [
    "https://camila-realm.auth.eu-west-1.amazoncognito.com/logout"
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cognito_user_pool_domain
resource "aws_cognito_user_pool_domain" "camila_domain" {
  domain       = var.camila_domain
  user_pool_id = aws_cognito_user_pool.camila_services_pool.id
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cognito_user
resource "aws_cognito_user" "camila_user" {
  user_pool_id             = aws_cognito_user_pool.camila_services_pool.id
  username                 = var.camila_user
  desired_delivery_mediums = ["EMAIL"]

  attributes = {
    email          = var.camila_user
    email_verified = "true"
  }

  client_metadata = {
    email          = var.camila_user
    email_verified = "true"
    name           = "Camila User"
  }
}
