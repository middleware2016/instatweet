#!/usr/bin/env bash

CURRENT_PATH=`pwd`

rmiregistry -J-Djava.rmi.server.codebase=file://${CURRENT_PATH}/rmicodebase/