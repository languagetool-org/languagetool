# Dockerfile

Dockefile is created on top of `maven:3.5-jdk-8` image.

# Requirements

 - The project should be cloned from https://github.com/trilogy-group/languagetool
 - Docker version 18.06.1-ce
 - Docker compose version 1.22.0
  
# Quick Start

- Unzip `languagetool-docker.zip` in `{project-root-folder}/`
- Open a terminal session to that folder
- Run `docker-compose build`
- Run `docker-compose up -d`
- Run `docker-compose exec builder bash`
- At this point you must be inside the docker container, in the root folder of the project. From there, you can run the commands as usual (described into the main project README.md)
	
- When you finish working with the container, type `exit`
- Run `docker-compose down` to stop the service.

# Work with the Docker Container

Copy this deliverable (`Dockerfile`, `docker-compose.yml`, `.dockerignore`) to `{project-root-folder}/`

## Build the image

In `{project-root-folder}/` folder, run:

```bash
docker-compose build
```

This instruction will create a DockerImage in your machine called `languagetool_builder:latest`

## Run the container

In `{project-root-folder}/` folder, run:

```bash
docker-compose up -d
```

Parameter `-d` makes the container run in detached mode.
This command will create a running container in detached mode called `builder`.
You can check the containers running with `docker ps`

## Get a container session

In `{project-root-folder}/` folder, run:

```bash
docker-compose exec builder bash
```

## docker-compose.yml

The docker-compose.yml file contains a single service: `builder`.
We will use this service to build the LanguageTool sources from our local environment, so we mount root project dir `.` to the a `/data` folder:

```yaml
    volumes:
      - .:/data:Z
```
