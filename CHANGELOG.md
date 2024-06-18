# Changelog

---

## [1.0.0] (feature/add_cognito_oauth)

### Added
- **OAuth Integration**: Added AWS Cognito OAuth2 support for authentication and authorization.
  - Implemented Cognito OAuth2 configuration in `application.yml`.
  - Updated security configuration to use Cognito OAuth2.
  - Added endpoints for login and token refresh using Cognito.

### Changed
- **Security Configuration**: Refactored security settings to accommodate OAuth2 integration.
  - Modified existing security filters to include OAuth2 client.
  - Updated security dependencies in `pom.xml`.

### Fixed
- **Minor Bugs**: Fixed various minor issues to ensure smooth OAuth2 integration.

### Documentation
- **Updated README**: Added section on how to configure and use AWS Cognito OAuth2 with the service.

---
