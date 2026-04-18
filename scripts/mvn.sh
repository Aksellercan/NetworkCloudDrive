#!/bin/sh
if [ $# = 1 ]; then
    mvn -DnewVersion="$1" versions:set
else
    echo "invalid. usage [VERSION]"
fi
