#!/usr/bin/env bash

appclient -client \
	producer/build/libs/producer.jar \
	jms/instatweet_connection_factory \
	jms/input_queue \
	timeline/build/libs/timeline.jar