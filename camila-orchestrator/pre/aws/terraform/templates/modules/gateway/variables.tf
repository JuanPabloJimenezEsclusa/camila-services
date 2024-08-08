variable "domain_name" {
  type        = string
  description = "The domain name for the Route 53 record set"
}

variable "user_pool_arn" {
  type        = string
  description = "The ARN for the user pool"
}

variable "gateway_hosted_zone_id" {
  type        = string
  description = "The Route 53 hosted zone ID for the gateway domain name"
  default     = "Z0854820KZ2JVLZ9E8PO"
}

variable "gateway_domain_name" {
  type        = string
  description = "The domain name for the API Gateway"
  default     = "jpje.net"
}
