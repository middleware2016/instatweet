#!/usr/bin/env bash

echo "Launching rmiregistry..."
(rmiregistry -J-Djava.rmi.server.codebase=file://$(pwd)/utility/build/classes/main/ &) && echo "rmiregistry launched and running in background."