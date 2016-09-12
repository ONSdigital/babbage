#!/bin/bash

### 1 - BUILD WEB FRONT-END

./build-web.sh

### 2 - BUILD API
./build-api.sh 

### 3- Start Highcharts export server


if [ ! -d highcharts ]
   then
    echo "Downloading highchart export server"
    git clone --depth 1 -b v4.1.6 https://github.com/highslide-software/highcharts.com.git highcharts
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

export JAVA_OPTS="-Xdebug -Xmx256m -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"

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

export PHANTOMJS_PATH=`which phantomjs`
export DEV_ENVIRONMENT="Y"
export IS_PUBLISHING="Y"
export RELOAD_TEMPLATES="Y"
export TEMPLATES_DIR=src/main/web/templates/handlebars

# true to enable throttling false to disable.
export SLACK_THROTTLE_ENABLED="false"
# Max number of slack notification tokens available.
export MAX_NOTIFICATION_TOKENS=10
# Time in MS before a new token is added.
export MS_UNTIL_NEW_NOTIFICATION_TOKEN=60000
# The Slack channel URL.
export SLACK_FEEDBACK_CHANNEL_URL="/services/T02KLUDD9/B23HZ3K1B/kYzvmO4K9cdDSGRTMKm7SNEb"
# The slack URL.
export SLACK_FEEDBACK_NOTIFICATION_HOST="https://hooks.slack.com"
# The directory to store the user feedback json files in.
export FEEDBACK_FOLDER=/Users/dave/Desktop/feedback
# The directory path to the RSA public key used to encrypt user feedback.
export PUBLIC_KEY=

# Development: reloadable
java $JAVA_OPTS \
 -Dpublic_key=$PUBLIC_KEY \
 -Dfeedback_folder=$FEEDBACK_FOLDER \
 -Dmax_notification_tokens=$MAX_NOTIFICATION_TOKENS \
 -Dms_until_new_notification_token=$MS_UNTIL_NEW_NOTIFICATION_TOKEN \
 -Dslack_feedback_channel_url=$SLACK_FEEDBACK_CHANNEL_URL \
 -Dslack_feedback_notification_host=$SLACK_FEEDBACK_NOTIFICATION_HOST \
 -Dslack_throttle_enabled=$SLACK_THROTTLE_ENABLED \
 -Drestolino.realm=$REALM \
 -Drestolino.files=$RESTOLINO_STATIC \
 -Drestolino.classes=$RESTOLINO_CLASSES \
 -Drestolino.packageprefix=$PACKAGE_PREFIX \
 -cp "target/dependency/*" \
 com.github.davidcarboni.restolino.Main

kill $exportserverpid
# Production: non-reloadable
#java $JAVA_OPTS -jar target/*-jar-with-dependencies.jar
