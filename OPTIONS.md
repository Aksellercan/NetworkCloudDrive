# Options, Sorts and Filters

***Thumbnails only applicable for these mimetypes***
### Images
- image/jpeg
- image/png
- image/webp
- image/vnd.microsoft.icon
- image/avif

### Videos *(Work in progress)*
- video/webm
- video/mpeg
- video/mp4

## Scan options
- GO_INTO_FOLDERS
- DONT_GO_INTO_FOLDERS
- ONLY_FOLDERS
- ONLY_FILES
- NORMAL

### Thumbnail scan options
- CREATE_THUMBNAILS
- DONT_CREATE_THUMBNAILS
- ONLY_THUMBNAILS

# Descriptions
*Scans might take a while*
- *Tested on `Intel(R) Core(TM) i5-3337U (4) @ 2.70 GHz` laptop*

### GO_INTO_FOLDERS, NORMAL, ONLY_THUMBNAILS
Default behaviour, scans directory recursively and creates thumbnails for applicable files
### DONT_GO_INTO_FOLDERS
Non-recursive only scans files and creates thumbnails inside directory doesn't enter folders inside the directory
### ONLY_FOLDERS
Only scans for folders, recursive
### ONLY_FILES
Only scans for files, non-recursive
### DONT_CREATE_THUMBNAILS
scans in NORMAL behaviour but won't create thumbnails
### ONLY_THUMBNAILS
Looks for files with applicable mimetypes and `has_thumbnail=false` in database and attempts to create thumbnails for them

## Sort options
- DEFAULT
- ALPHABETICAL
- REVERSE_ALPHABETICAL
- NEWEST
- OLDEST
- FOLDERS_FIRST
- SIZE_LOWEST
- SIZE

# Descriptions
### DEFAULT
Default sorting of the OS API runs on. Since the file names are encoded this sorting depends on OS
### ALPHABETICAL
A-Z sorting using decoded filenames
### REVERSE_ALPHABETICAL
Z-A sorting using decoded filenames
### NEWEST
Returns newest first
### OLDEST
Returns oldest first
### FOLDERS_FIRST
Positions folders first in list returned
### SIZE_LOWEST
Returns smallest files first
### SIZE
Returns biggest files first

## Filter options
- TYPE - *Not implemented yet*
- KEYWORD
- FILES_ONLY
- FOLDERS_ONLY

# Descriptions
### KEYWORD
Returns values containing the keyword. Requires another parameter called `filter` and a `keyword` string
### FILES_ONLY
Only returns files
### FOLDERS_ONLY
Only returns folders