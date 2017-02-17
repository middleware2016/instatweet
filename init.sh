#!/usr/bin/env bash

asadmin start-domain

asadmin create-jms-resource --restype javax.jms.ConnectionFactory --property useSharedSubscriptionInClusteredContainer=false jms/instatweet_connection_factory
asadmin create-jms-resource --restype javax.jms.Queue --property Name=PhysicalQueue jms/input_queue
asadmin create-jms-resource --restype javax.jms.Queue --property Name=PhysicalQueue jms/image_queue
asadmin create-jms-resource --restype javax.jms.Queue --property Name=PhysicalQueue jms/dispatch_queue

asadmin stop-domain