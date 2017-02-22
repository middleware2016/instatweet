#!/usr/bin/env bash

gradle build

mkdir -p rmicodebase
cp utility/build/classes/main/interfaces/*Interface.class utility/build/classes/main/payloads/Tweet.class rmicodebase

