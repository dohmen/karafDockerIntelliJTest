FROM apache/karaf:4.2.8

COPY ./target/karafDockerIntelliJTest*jar /tmp/karafDockerIntelliJTest.jar
COPY ./karaf/feature.xml /opt/apache-karaf/deploy/

ENTRYPOINT ["/opt/apache-karaf/bin/karaf", "debug"]

