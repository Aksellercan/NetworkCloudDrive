#!/bin/sh

increment_minor() {
    local cut=$(echo "$1" | cut -d "." -f 3)
    local new_version=$((cut + 1))
}

increment_major() {
    local cut=$(echo "$1" | cut -d "." -f 2)
    local new_version=$((cut + 1))
    echo "${1//.[0-9]/"$new_version"}"
}

release_version() {
    local new_version=''
}

snapshot_version() {
    local new_version=''
}

update_pom() {
    mvn -DnewVersion="$2" versions:set
}

loop_over_string() {
    local foo=$1
    for (( i=0; i<${#foo}; i++ )); do
        echo "${foo:$i:1}"
    done
}

if [ $# = 2 ]; then
    if [ $1 == "-m" ]; then
        increment_minor $2
    elif [ $1 == "-M" ]; then
        increment_major $2
    elif [ $1 == "-r" ]; then
        release_version $2
    elif [ $1 == "-s" ]; then
        snapshot_version $2
    fi
else
    echo "Invalid usage. USAGE [OPTION] [ARGUMENT]"
fi
