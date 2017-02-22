#!/usr/bin/env bash

appclient -client \
	loadmanager/build/libs/loadmanager.jar \
	jms/instatweet_connection_factory \
	jms/dispatch_queue \
	timeline/build/libs/timeline.jar \
	database/build/libs/database.jar \
	dispatcher/build/libs/dispatcher.jar