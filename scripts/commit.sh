#!/bin/bash

function help(){
    python3 ./scripts/mvn.py --help
}

if ! [ -f pom.xml ]; then
    echo "Run script from same directory as pom.xml"
    exit 1
fi

if [ $# == 0 ]; then
    help
else
    python3 ./scripts/mvn.py $1
fi

git status
