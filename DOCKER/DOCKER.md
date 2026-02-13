# Run via Docker File

[Look inside Docker file](../Dockerfile)

## Build Options

```bash
cd /path/to/NetworkCloudDrive
docker build -t networkclouddrive .
```
*Note you can set container name to anything after ```-t``` or not include it and let docker name it*

## Run Options

```bash
docker run --network=host networkclouddrive:latest
```
- ```--network=host``` allows connections from devices in current network

- You can also forward ports to get the same functionality

*Note the parameter might not be necessary in the future*

# Updating API

Updating API will reset the container, there are 2 scripts available to automate data backup and restore:

## Backup Files

- Run ```backupFiles.sh container_name```, if no parameter provided script won't run
- Script will copy contents of Docker container and will create folder called ```NetworkCloudDriveDocker_$time_stamp```

## Restore Files

- Run ```restoreFiles.sh container_name```, if no parameter provided script won't run
- Script will look for folder starting with ```NetworkCloudDriveDocker_*``` and pick the latest
- Then it will restore files to docker container
 
# Updating Configuration After Deploying

1. Get Docker Container ID:

```bash
docker ps -a
```

2. Find ```networkclouddrive:latest``` in Image column and left to it is Container ID

3. Create folder called ```config``` then inside create ```application.properties``` file

4. Edit the file then run:

```bash
docker cp config [CONTAINER_ID]:/app
```

5. Restart the container

```bash
docker start [CONTAINER_ID]
```

Now your changes should be in effect.