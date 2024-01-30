#!/bin/bash

### 1 - BUILD WEB FRONT-END

./build-web.sh

### 2 - BUILD API
./build-api.sh

### 3 - START BABBAGE
export JAVA_OPTS="-Xmx512m -Xdebug -Xrunjdwp:transport=dt_socket,address=8010,server=y,suspend=n"

# Restolino configuration
export RESTOLINO_STATIC="src/main/web"
export RESTOLINO_CLASSES="target/classes"
export PACKAGE_PREFIX=com.github.onsdigital

export DP_COLOURED_LOGGING=true
export DP_LOGGING_FORMAT=pretty_json

export DEV_ENVIRONMENT="Y"
export RELOAD_TEMPLATES="Y"
export TEMPLATES_DIR=src/main/web/templates/handlebars
export ENABLE_COVID19_FEATURE=false
export ENABLE_NAVIGATION="Y"
export TOPICS_SERVICE_URL="http://localhost:25300"

# Development: reloadable
java $JAVA_OPTS \
 -Drestolino.realm=$REALM \
 -Drestolino.files=$RESTOLINO_STATIC \
 -Drestolino.classes=$RESTOLINO_CLASSES \
 -Drestolino.packageprefix=$PACKAGE_PREFIX \
 -javaagent:target/dependency/aws-opentelemetry-agent-1.31.0.jar \
 -Dotel.propagators=tracecontext,baggage \
 -Dotel.javaagent.enabled=false \
 -cp "target/classes/:target/dependency/*" \
 com.github.davidcarboni.restolino.Main