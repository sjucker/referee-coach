# Referee Coach

## Development

### Commands

* Start DB in Docker container:  
  `docker compose -p referee-coach -f src/main/docker/mysql.yml down && docker compose -p referee-coach -f src/main/docker/mysql.yml up --build`

## Heroku

### Pipeline

There are 2 apps in the pipeline:

* **STAGING**: `develop` branch
* **PRODUCTION**: `main` branch

They use the same codebase, but have different config vars (Heroku Dashboard -> Settings).  
Also, for the STAGING-app we must activate another Maven profile, so the frontend-maven-plugin runs the correct build-configuration (that uses
the `environment.staging.ts`). If no specific Maven profile is defined, production build is triggered. The profile is activated using the `MAVEN_CUSTOM_OPTS`
config var (only needed for STAGING).
