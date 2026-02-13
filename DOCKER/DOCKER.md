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