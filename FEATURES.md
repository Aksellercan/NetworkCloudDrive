# Current Features

## Query Metadata

| Method | 	Route                                     | 	Description                   | Working            |
|--------|--------------------------------------------|--------------------------------|--------------------|
| GET    | 	api/info/get/filemetadata?fileid={id}     | 	Returns File Metadata by ID   | :heavy_check_mark: |
| GET    | 	api/info/get/foldermetadata?folderid={id} | 	Returns Folder Metadata by ID | :heavy_check_mark: |

## FileSystem File Endpoints

| Method | 	Route                                  | 	Description                                  | Working            |
|--------|-----------------------------------------|-----------------------------------------------|--------------------|
| POST   | 	api/filesystem/file/move               | Moves file given Id to another folder by ID   | :heavy_check_mark: |
| POST   | 	api/filesystem/file/remove?fileid={id} | Removes file by ID                            | :heavy_check_mark: |
| POST   | 	api/filesystem/file/rename             | Renames file with ID parameter with JSON body | :heavy_check_mark: |

## FileSystem Folder Endpoints

| Method | 	Route                                      | 	Description                                    | Working            |
|--------|---------------------------------------------|-------------------------------------------------|--------------------|
| POST   | 	api/filesystem/folder/move                 | Moves folder given Id to another folder by ID   | :heavy_check_mark: |
| POST   | 	api/filesystem/folder/remove?folderid={id} | Removes folder by ID along with child folders   | :heavy_check_mark: |
| POST   | 	api/filesystem/folder/rename               | Renames folder with ID parameter with JSON body | :heavy_check_mark: |

## File Endpoints

| Method | 	Route                         | 	Description                                | Working            |
|--------|--------------------------------|---------------------------------------------|--------------------|
| GET    | 	api/file/download?fileid={id} | Finds and downloads file by ID              | :heavy_check_mark: |
| POST   | 	api/file/create/folder        | Creates folder with name at given folder ID | :heavy_check_mark: |
| POST   | 	api/file/upload               | Uploads file(s) at folder ID path           | :heavy_check_mark: |

## List Structure Endpoint

| Method | 	Route                             | 	Description                          | Working            |
|--------|------------------------------------|---------------------------------------|--------------------|
| GET    | 	api/filesystem/list?folderid={id} | Returns files/folders inside a folder | :heavy_check_mark: |

## User Endpoints

| Method | 	Route                    | 	Description                                                 | Working            |
|--------|---------------------------|--------------------------------------------------------------|--------------------|
| POST   | 	/login                   | User login                                                   | :heavy_check_mark: |
| POST   | 	api/user/register        | Registers user                                               | :heavy_check_mark: |
| GET    | 	api/user/info            | Returns User details                                         | :heavy_check_mark: |
| PUT    | 	api/user/update/mail     | Updates user mail                                            | :heavy_check_mark: |
| PUT    | 	api/user/update/name     | Updates user name                                            | :heavy_check_mark: |
| PUT    | 	api/user/update/password | Updates and hashes user password                             | :heavy_check_mark: |
| DELETE | 	api/user/delete          | Deletes User from database and folders belonging to the user | :heavy_minus_sign: |

Use PUT if request body is same as route's accepted model. PATCH if parts of route's accepted model.

## Maintenance Endpoints

coming soon...