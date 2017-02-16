# instatweet
A social networking application that uses JMS.

## TODO
- create an init script
- create a run script


## Software needed
Download a version of GlassFish server

Set the env adding `asadmin` and `appclient` from GlassFish

## How to build

## How to run (manual)
Start GlassFish server and add resources
    
    asadmin start-domain
    asadmin create-jms-resource --restype javax.jms.ConnectionFactory --property useSharedSubscriptionInClusteredContainer=false jms/instatweet_connection_factory
    asadmin create-jms-resource --restype javax.jms.Queue --property Name=PhysicalQueue jms/input_queue
    asadmin create-jms-resource --restype javax.jms.Queue --property Name=PhysicalQueue jms/image_queue
    asadmin create-jms-resource --restype javax.jms.Queue --property Name=PhysicalQueue jms/dispatch_queue
    
If you want to stop the domain use
    
    asadmin stop-domain
     
     
Launch RMI registry with all the Remote interfaces in the codebase

Go to instatweet main folder and run the following command

    appclient -client loadmanager/build/libs/loadmanager.jar jms/instatweet_connection_factory jms/input_queue jms/dispatch_queue jms/image_queue timeline/build/libs/timeline.jar inputserver/build/libs/inputserver.jar database/build/libs/database.jar dispatcher/build/libs/dispatcher.jar imagehandler/build/libs/imagehandler.jar

##Additional commands
In order to separately run the jars for testing purposes use the following commands

    appclient -client inputserver/build/libs/inputserver.jar jms/instatweet_connection_factory jms/input_queue jms/dispatch_queue jms/image_queue timeline/build/libs/timeline.jar instatweet_database input_server_0
    java -jar database/build/libs/database.jar instatweet_database
    appclient -client dispatcher/build/libs/dispatcher.jar jms/instatweet_connection_factory jms/dispatch_queue dispatcher_0



