# Changelog
All notable changes in keycloak-idp-hashedId-mapper will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

For Keycloak upstream changelog please see https://www.keycloak.org/docs/latest/release_notes/index.html.
Full Keycloak upstream jira issue can be shown if filtered by Fix version. For example [Keycloak jira issue for 15.0.2 version](https://issues.redhat.com/browse/KEYCLOAK-19161?jql=project%20%3D%20keycloak%20and%20fixVersion%20%3D%2015.0.2)

## [2.1.0] - 23-09-2024

### Changed
- Configured value for voPersonId

## [2.1.0] - 10-7-2024
### Added
- Add idpName together with authnAuthority

### Changed
- Support for Keycloak version over 22.0.10-1.8

## [2.0.0] - 1-2-2022
### Changed
- Support for Keycloak version 22

## [1.2.0] - 1-2-2022
### Added
- The plugin supports all SAML IdPs, all OIDC-based IdPs (i.e. google), and all OAUTH2-based IdPs (facebook, github, etc).

### Changed
- The plugin now is hot-deployable. Discarded the module.xml file.

