variable "hosted_zone_id" {
  description = "The ID of the Route 53 hosted zone"
  default     = "Z102528520PCT47CK313B"
}

variable "domain_name" {
  type        = string
  description = "The domain name for the Route 53 record set"
}

variable "image_name" {
  description = "The name of the Docker image"
  default     = "546053716955.dkr.ecr.eu-west-1.amazonaws.com/camila-product-api:1.0.0"
}

variable "repository_technology" {
  description = "The repository technology"
  default     = "mongo"
}

variable "couchbase_connection" {
  description = "The Couchbase connection string"
  type        = string
  sensitive   = true
}

variable "couchbase_username" {
  description = "The Couchbase username"
  type        = string
  sensitive   = true
}

variable "couchbase_password" {
  description = "The Couchbase password"
  type        = string
  sensitive   = true
}

variable "mongo_uri" {
  description = "The MongoDB URI"
  type        = string
  sensitive   = true
}

variable "user_pool_id" {
  description = "The Cognito User Pool ID"
  type        = string
}