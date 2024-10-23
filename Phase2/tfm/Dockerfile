FROM tomcat:10.1.16-jdk21-temurin-jammy
RUN sed -i 's/port="8080"/port="16003"/' ${CATALINA_HOME}/conf/server.xml

ADD target/tspa-service.war /usr/local/tomcat/webapps/tspa-service.war
COPY tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
COPY context.xml /usr/local/tomcat/webapps/manager/META-INF/context.xml


CMD ["catalina.sh", "run"]

