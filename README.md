# keycloak-metrics-plugin
A pluggable metrics events plugin for keycloak. To be used together with metrics 

## Installation instructions :

1. Compile the plugin jar i.e. 'mvn clean install' or just get a built one from the "Releases" link on the right sidebar.
2. Drop the jar into the folder $KEYCLOAK_BASE/providers and let all the hot-deploy magic commence.


## Use instructions

#### Realm attributes needed configuration

You need to define the following realm attributes in order Keycloak metric plugin to be able to communicate with Metrics:
- amsUrl : ams url
- X-Api-Key : ams token
- tenenvId : environment parameter
- source : Keycloak
- keycloakUrl : Keycloak main url

Moreover, you need to add in Events Config -> Event Listeners the "metrics-communication".

## Keycloak compatibility matrix
| Group management version | Keycloak version        |
|--------------------------|-------------------------|
| 1.2.0                    | 18.0.1-2.17             |
| 2.0.0                    | 22.0.5-1.1 - 22.0.10-1.7 |
| 2.1.0                    | 22.0.10-1.8 +           |