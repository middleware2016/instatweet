#!/usr/bin/env bash

gradle build

mkdir -p rmicodebase
cp {database,dispatcher,imagehandler,inputserver,timeline,loadmanager}/build/classes/main/*Interface.class utility/build/classes/main/Tweet.class rmicodebase

