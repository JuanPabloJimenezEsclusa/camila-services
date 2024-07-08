
# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/caller_identity
data "aws_caller_identity" "current" {}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/availability_zones
data "aws_availability_zones" "available" {}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/region
data "aws_region" "current" {}
