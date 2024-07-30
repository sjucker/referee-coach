# Referee Coach

## Development

### Commands

* Start DB in Docker container:  
  `docker compose -p referee-coach -f src/main/docker/postgres.yml down && docker compose -p referee-coach -f src/main/docker/postgres.yml up --build`

## Heroku

### Pipeline

There are 2 apps in the pipeline:

* **STAGING**: `develop` branch
* **PRODUCTION**: `main` branch

They use the same codebase, but have different config vars (Heroku Dashboard -> Settings).  
Also, for the STAGING-app we must activate another Maven profile, so the frontend-maven-plugin runs the correct build-configuration (that uses
the `environment.staging.ts`). If no specific Maven profile is defined, production build is triggered. The profile is activated using the `MAVEN_CUSTOM_OPTS`
config var (only needed for STAGING).

## DB Migration

-- docker run --rm -it ghcr.io/dimitri/pgloader:latest pgloader mysql://probasket7:yWn7Tym0Isq8elM9BCm7DQKvHFgsjBcP@db8.netzone.ch:3306/probasket7 postgresql:
//ufff9aoodhj7ac:p01fa39240981586a37f0287343b2bbd95bc87f875855dee0c88953830e70288b@c3l5o0rb2a6o4l.cluster-czz5s0kz4scl.eu-west-1.rds.amazonaws.com:
5432/ddhc44pdomci4s?sslmode=allow
