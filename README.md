# Referee Coach

## Development

### Commands

* Start DB in Docker container:  
  `docker compose -p referee-coach -f src/main/docker/mysql.yml down && docker compose -p referee-coach -f src/main/docker/mysql.yml up --build`
* For test:
  `docker compose -p referee-coach -f src/test/docker/mysql-test.yml up --build`

## Heroku

### Pipeline

There are 2 apps in the pipeline:

* **STAGING**: `develop` branch
* **PRODUCTION**: `main` branch

They use the same codebase, but have different config vars (Heroku Dashboard -> Settings).  
Also, for the STAGING-app we must activate another Maven profile, so the frontend-maven-plugin runs the correct build-configuration (that uses
the `environment.staging.ts`). If no specific Maven profile is defined, production build is triggered. The profile is activated using the `MAVEN_CUSTOM_OPTS`
config var (only needed for STAGING).

## Updates

* Maven Wrapper
    * `mvn wrapper:wrapper -Dmaven=3.9.11`
* Update Maven Parent
    * `mvn -U versions:display-parent-updates`
    * `mvn -U versions:update-parent`
* Update Versions in Properties
    * `mvn -U versions:display-property-updates`
    * `mvn -U versions:update-properties`
* Download Sources
    * `mvn dependency:resolve-sources`
* Build
    * `mvn clean verify -DskipTests=true`

* Angular
    * `ng update @angular/core@20 @angular/cli@20 --allow-dirty`
    * `ng update @angular/material@20 --allow-dirty`
    * `ncu`
    * `ncu -u`, or
    * `ncu -i` (for interactive update)
    * `npm install`
    * `npm run build`
    * `npm run lint`
