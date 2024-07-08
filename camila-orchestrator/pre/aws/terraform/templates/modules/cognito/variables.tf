variable "domain_name" {
  type        = string
  description = "The domain name for the Route 53 record set"
}

variable "camila-realm" {
  type        = string
  default     = "camila-realm"
  description = "Name of the User Pool"
}

variable "camila_log_group" {
  type        = string
  default     = "/camila/services/cognito-logs"
  description = "Cognito Log Group"
}

variable "camila_domain" {
  type        = string
  default     = "camila-realm"
  description = "Cognito User Pool Domain"
}

variable "camila_user" {
  type        = string
  default     = "olbapnuaj@gmail.com"
  description = "Email of the initial user"
}
