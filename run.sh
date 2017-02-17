#!/usr/bin/env bash

appclient -client loadmanager/build/libs/loadmanager.jar jms/instatweet_connection_factory jms/input_queue jms/dispatch_queue jms/image_queue timeline/build/libs/timeline.jar inputserver/build/libs/inputserver.jar database/build/libs/database.jar dispatcher/build/libs/dispatcher.jar imagehandler/build/libs/imagehandler.jar