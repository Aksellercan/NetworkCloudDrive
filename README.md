# Network Cloud Drive

Local network storage API that can store files and manage them. Uses SQLite for retrieving files and finding paths fast

[FEATURES](FEATURES.md)

## Future plans
- React based Frontend for Desktop and iOS/Android phones
- Routinely syncing database with filesystem and a way to force a resync

# Run

- [Using Docker](DOCKER/DOCKER.md)

# Configuration

application.properties configurations can be overridden by creating file at ./config/application.properties

Example configurations:
- Configure CORS
- Enable HTTP Basic Authentication
- Enable logging debug level
- Disable CSRF Protection
- Change Springboot port

... and more

# Helper Tools
- [iOS Shortcut](https://www.icloud.com/shortcuts/c254fe91c45545bfa56d5815688f8770). Create note called `HTTP Auth` with format like:
```
username:
[mail]
password:
[password]
default_folderid:
[folderid-to-save-to]
```
then using share menu you can upload files without even downloading the file.

## File Structure Visual

```mermaid
flowchart TD
    A[Project Path] -->|Storage| B{root/base}
    B -->|User 1 storage| C[User 1]
    B -->|User 2 storage| D[User 2]
    B -->|User 3 storage| E[User 3]
```
