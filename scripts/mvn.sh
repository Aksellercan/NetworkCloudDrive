#!/bin/sh
if [ $# = 2]; then
    if [ $1 == "-m" ]; then
        increment_minor
    elif [ $1 == "-M" ]; then
        increment_major
    elif [ $1 == "-r" ]; then
        release_version
    elif [ $1 == "-s" ]; then
        snapshot_version
    fi
else
    echo "Invalid usage. USAGE [OPTION] [ARGUMENT]"
fi

increment_minor() {
    local cut=$(echo "$2" | cut -d "." -f 3)
    local new_version=$()
}

increment_major() {
    local new_version = ''
}

release_version() {
    local new_version = ''
}

snapshot_version() {
    local new_version = ''
}

update_pom() {
    mvn -DnewVersion="$1" versions:set
}
