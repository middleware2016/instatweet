#!/usr/bin/env bash

asadmin start-domain

asadmin create-jms-resource --restype javax.jms.ConnectionFactory --property useSharedSubscriptionInClusteredContainer=false jms/instatweet_connection_factory
asadmin create-jms-resource --restype javax.jms.Queue --property Name=DispatchQueue jms/dispatch_queue

asadmin stop-domain