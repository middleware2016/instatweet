#!/usr/bin/env bash

echo "Launching rmiregistry..."
(rmiregistry -J-Djava.rmi.server.codebase=file://$(pwd)/rmicodebase/ &) && echo "rmiregistry launched and running in background."