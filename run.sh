#!/bin/bash

### 1 - BUILD WEB FRONT-END

./build-web.sh

### 2 - BUILD API
./build-api.sh "$1" 

### 3- Start Highcharts export server


if [ ! -d highcharts ]
   then
    echo "Downloading highchart export server"
    git clone --depth 1 -b v4.1.6 git@github.com:highslide-software/highcharts.com.git highcharts
   else
       echo "Highcharts already available. will not download"
fi

export EXPORT_SERVER_DIR="highcharts/exporting-server/java/highcharts-export"
export EXPORT_SERVER_WEB="highcharts-export-web"
export CWD=`pwd`
export JAVA_OPTS="-Xrunjdwp:transport=dt_socket,address=9000,server=y,suspend=n"

cd $EXPORT_SERVER_DIR && \
mvn  clean install && \
cd $EXPORT_SERVER_WEB && \
echo -e "\nexec = $CWD/src/main/web/node_modules/phantomjs/bin/phantomjs"  >> src/main/webapp/WEB-INF/spring/app-convert.properties

if [ $? -eq 0 ]
    then
      echo "Everything alright, starting the server"
    else
       echo "Something went wrong, server will not be started"
       exit 1
 fi

cd $CWD
mvn -f "$EXPORT_SERVER_DIR/$EXPORT_SERVER_WEB/pom.xml" -Djetty.port=9999 -Dlog4j.logger.exporter=DEBUG jetty:run > export-server.log 2>&1&
exportserverpid=$!
echo "export server pid: $exportserverpid"

### 4 - START BABBAGE

export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"

#External Taxonomy
#export TAXONOMY_DIR=target/content

# Restolino configuration
export RESTOLINO_STATIC="src/main/web"
export RESTOLINO_CLASSES="target/classes"
export PACKAGE_PREFIX=com.github.onsdigital

# For testing out HTTP basic auth
#export USERNAME=user
#export PASSWORD=password
#export REALM=onsalpha

# Elasticsearch
export BONSAI_URL=http://localhost:9200
export BONSAI_CLUSTERNAME=elasticsearch
export BONSAI_HOSTNAME=localhost
export BONSAI_TRANSPORT_PORT=9300

# Mongodb
export MONGO_USER=ons
export MONGO_PASSWORD=uJlVY2FDGI5SFawS/PN+jnZpymKWpU7C

# Development: reloadable
$JAVA_HOME/bin/java $JAVA_OPTS \
 -Drestolino.realm=$REALM \
 -Drestolino.files=$RESTOLINO_STATIC \
 -Drestolino.classes=$RESTOLINO_CLASSES \
 -Drestolino.packageprefix=$PACKAGE_PREFIX \
 -Dmongo.user=$MONGO_USER \
 -Dmongo.password=$MONGO_PASSWORD \
 -cp "target/dependency/*" \
 com.github.davidcarboni.restolino.Main

kill $exportserverpid
# Production: non-reloadable
#$JAVA_HOME/bin/java $JAVA_OPTS -jar target/*-jar-with-dependencies.jar
