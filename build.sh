#!/usr/bin/env bash

gradle build

mkdir -p rmicodebase
cp {database,dispatcher,imagehandler,inputserver,timeline,loadmanager}/build/classes/main/*Interface.class rmicodebase

