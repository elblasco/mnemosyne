FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn -B -q -e dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM tomcat:10.1-jdk17-temurin

RUN apt-get update && apt-get install -y sqlite3 && rm -rf /var/lib/apt/lists/*
COPY init-db.sh /usr/local/bin/init-db.sh
RUN chmod +x /usr/local/bin/init-db.sh
VOLUME ["/data"]

RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=builder /app/target/Mnemosyne-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/mnemosyne.war
COPY server-keystore.p12 /usr/local/tomcat/conf/keystore.p12
RUN sed -i 's|</Service>|\
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol" \
           maxThreads="150" SSLEnabled="true">\
    <SSLHostConfig>\
        <Certificate certificateKeystoreFile="conf/keystore.p12" \
                     certificateKeystorePassword="qwerty123" \
                     type="EC" />\
    </SSLHostConfig>\
</Connector>\
</Service>|' /usr/local/tomcat/conf/server.xml

EXPOSE 8443

CMD ["/bin/bash", "-c", "rm -rf /data/* && /usr/local/bin/init-db.sh && catalina.sh run"]

LABEL description="Connect to https://localhost:8443/mnemosyne/"