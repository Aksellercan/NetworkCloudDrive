#/bin/bash
rm -rf ./root #remove entire folder and contents
echo "removed root folder"

rm filedatabase.db
echo "removed db file"

#can combine rm command into single line: rm -rf ./root filedatabase.db