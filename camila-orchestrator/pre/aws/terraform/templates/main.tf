terraform {
  required_version = ">= 1.9.1"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.55.0"
    }
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs
provider "aws" {
  region = "eu-west-1"
}

variable "domain_name" {
  type        = string
  description = "The domain name for the Route 53 record set"
  default     = "poc.jpje-kops.xyz"
}

variable "couchbase_connection" {
  description = "Couchbase connection string"
  type        = string
  sensitive   = true
}

variable "couchbase_username" {
  description = "Couchbase username"
  type        = string
  sensitive   = true
}

variable "couchbase_password" {
  description = "Couchbase password"
  type        = string
  sensitive   = true
}

variable "mongo_uri" {
  description = "MongoDB connection URI"
  type        = string
  sensitive   = true
}

module "camila_cognito_module" {
  source      = "./modules/cognito"
  domain_name = var.domain_name
}

module "camila_ecs_module" {
  source               = "./modules/ecs"
  domain_name          = var.domain_name
  couchbase_connection = var.couchbase_connection
  couchbase_username   = var.couchbase_username
  couchbase_password   = var.couchbase_password
  mongo_uri            = var.mongo_uri
  user_pool_id         = module.camila_cognito_module.user_pool_id
}

module "camila_gateway_module" {
  source      = "./modules/gateway"
  domain_name = var.domain_name
}

module "camila-rds-module" {
  source = "./modules/waf"
  camila_api_gateway_arn = module.camila_gateway_module.camila_aws_api_gateway_stage_arn
}
