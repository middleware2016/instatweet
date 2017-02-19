# instatweet
A social networking application that uses JMS.

## TODO
- add glassfish download to `init.sh` and merge some scripts



## Software needed
Download a version of GlassFish server

Set the env adding `asadmin` and `appclient` from GlassFish

## How to build

run `./init.sh`

run `./build.sh`

## How to run (manual)
Start GlassFish server and add resources
    
    asadmin start-domain
 
If you want to stop the domain use
    
    asadmin stop-domain
     
     
Launch RMI registry in a different shell from instatweet folder

    ./runrmi.sh

In the previous shell again from instatweet folder run the server

    ./runServer.sh
    
And then, the client:
   
    ./runClient.sh
    
## Additional commands
In order to separately run the jars for testing purposes use the following commands

    appclient -client loadmanager/build/libs/loadmanager.jar jms/instatweet_connection_factory jms/input_queue jms/dispatch_queue jms/image_queue timeline/build/libs/timeline.jar inputserver/build/libs/inputserver.jar database/build/libs/database.jar dispatcher/build/libs/dispatcher.jar imagehandler/build/libs/imagehandler.jar
    appclient -client inputserver/build/libs/inputserver.jar jms/instatweet_connection_factory jms/input_queue jms/dispatch_queue jms/image_queue timeline/build/libs/timeline.jar instatweet_database input_server_0
    java -jar database/build/libs/database.jar instatweet_database
    appclient -client dispatcher/build/libs/dispatcher.jar jms/instatweet_connection_factory jms/dispatch_queue dispatcher_0



