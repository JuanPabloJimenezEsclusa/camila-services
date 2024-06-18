terraform {
  required_version = ">= 1.8.5"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.54.1"
    }
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs
provider "aws" {
  region = "eu-west-1"
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

module "camila-cognito-module" {
  source = "./modules/cognito"
}

module "camila-ecs-module" {
  source               = "./modules/ecs"
  couchbase_connection = var.couchbase_connection
  couchbase_username   = var.couchbase_username
  couchbase_password   = var.couchbase_password
  mongo_uri            = var.mongo_uri
  user_pool_id         = module.camila-cognito-module.user_pool_id
}
