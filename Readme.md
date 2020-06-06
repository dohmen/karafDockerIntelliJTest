# IntelliJ IDEA: debug an OSGI bundle in Apache Karaf containerized with Docker 

* this sample shows how to debug an application running inside a container built and deployed by IntelliJ
* the container is provisioned with Apache Karaf and the application gets installed as a Karaf feature
* Maven based OSGi bundle project
* class com.com.example.MyEventLister: auto-wired EventAdmin, can be triggered from within Karaf Shell
* requires installed feature "scr" in Karaf (declarative services)

This was tested with IntelliJ IDEA 2020.1 (Ultimate).

## basic implementation of an OSGi service

MyEventLister is an EventHandler that can be installed with the resulting bundle JAR that is defined in pom.xml by using maven-bundle-plugin.

After packaging the JAR you can deploy it manually to Karaf:

```
karaf@root()> feature:install scr
karaf@root()> bundle:install -s file:///home/mdo/code/karafDockerIntelliJTest/target/karafDockerIntelliJTest-1.0-SNAPSHOT.jar
Bundle ID: 49
karaf@root()>
karaf@root()>
karaf@root()> bundle:list
START LEVEL 100 , List Threshold: 50
ID │ State  │ Lvl │ Version        │ Name
───┼────────┼─────┼────────────────┼──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
23 │ Active │  80 │ 4.2.2          │ Apache Karaf :: OSGi Services :: Event
49 │ Active │  80 │ 1.0.0.SNAPSHOT │ karafDockerIntelliJTest
```

I had decided to implement an EventHandler, because you can test it with Karaf tools directly from the shell 
(the handler just prints the event object's toString() result):

```
karaf@root()> event:send some/topic key1=test123
org.osgi.service.event.Event [topic=some/topic] {key1=test123, subject=Subject:
        Principal: UserPrincipal[karaf]
        Principal: ClientPrincipal[local(localhost)]
        Principal: RolePrincipal[admin]
        Principal: RolePrincipal[manager]
        Principal: RolePrincipal[viewer]
        Principal: RolePrincipal[systembundles]
, timestamp=1591385786822}
```

## dockerizing the application

In order to work with Docker from within IDEA you first  have to configure a Docker daemon connection:

![Docker settings](doc\Docker%20settings%20IDEA.png)

Jetbrains describes these settings there: https://www.jetbrains.com/help/idea/docker.html

Jetbrains also hosts a howto "Run a Java application in a Docker container":
https://www.jetbrains.com/help/idea/running-a-java-app-in-a-container.html

You can easily adapt a *Dockerfile* to base the new image on *apache/karaf* and copy a *feature.xml* along with our bundle JAR. 
Dropped into Karaf's deploy folder, our feature definition ensures that the bundle and pre-requirement feature "scr" are
 installed automatically.
 
```dockerfile
FROM apache/karaf:4.2.8

COPY ./target/karafDockerIntelliJTest*jar /tmp/karafDockerIntelliJTest.jar
COPY ./karaf/feature.xml /opt/apache-karaf/deploy/

ENTRYPOINT ["/opt/apache-karaf/bin/karaf"]
```

When IDEA discovers a new Dockerfile you'll be asked for settings:

![Dockerfile settings](doc\Dockerfile%20settings%20IDEA.png)

You should then be able to use this newly created run configuration to run your application.

There's a tool window "Services" in IDEA (ALT-8) that shows your Docker daemon and running containers.
After starting the application there a Container for the "Dockerfile" and you can watch
the build log tab:

```
Deploying '<unknown> Dockerfile: Dockerfile'...
Building image...
Preparing build context archive...
[==================================================>]18/18 files
Done

Sending build context to Docker daemon...
[==================================================>] 8,195kB
Done

Step 1/4 : FROM apache/karaf:4.2.8
 ---> e6e9f0f9c039
Step 2/4 : COPY ./target/karafDockerIntelliJTest*jar /tmp/karafDockerIntelliJTest.jar
 ---> Using cache
 ---> b4bb4881f691
Step 3/4 : COPY ./karaf/feature.xml /opt/apache-karaf/deploy/
 ---> Using cache
 ---> 6daead1642bb
Step 4/4 : ENTRYPOINT ["/opt/apache-karaf/bin/karaf", "debug"]
 ---> Using cache
 ---> 4023d5ddd6f8

Successfully built 4023d5ddd6f8
Creating container...
Container Id: e864af9ba1e52182dfb6d4f51d67d50d6ebc9ede2c513aef65d085928ac79b06
Container name: '/affectionate_herschel'
Attaching to container '/affectionate_herschel'...
Starting container '/affectionate_herschel'
'<unknown> Dockerfile: Dockerfile' has been deployed successfully.
```
As the entry point for the image is the Karaf command there's a shell in the foreground that you 
can find in the "Attached Console" tab:

![Attached Console](doc/IDEA%20Docker%20Attached%20Console.png)

## debugging

Jetbrains describes how to debug an application in a container:
https://www.jetbrains.com/help/idea/debug-a-java-application-using-a-dockerfile.html#

In the given scenario these are important aspects:
* start Karaf in debug mode (add entry point param in Dockerfile)
* expose debug port in container
* create remote debug configuration in IntelliJ
  * Socket connect to localhost:5005
  * add a Before Launch step for the Dockerfile
  
![debug remote settings](doc/IDEA%20Run%20Debug%20Configurations.png)

 