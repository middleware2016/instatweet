#!/usr/bin/env bash

gradle build

if [ ! -d "rmicodebase" ]; then
    mkdir rmicodebase
fi

yes | cp database/build/classes/main/DatabaseInterface.class rmicodebase
yes | cp dispatcher/build/classes/main/DispatcherInterface.class rmicodebase
yes | cp imagehandler/build/classes/main/ImageHandlerInterface.class rmicodebase
yes | cp inputserver/build/classes/main/InputServerInterface.class rmicodebase
yes | cp timeline/build/classes/main/TimelineInterface.class rmicodebase

