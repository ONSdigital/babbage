FROM openjdk:8-jre

# Add the build artifacts
WORKDIR /usr/src
ADD ./target/dependency /usr/src/target/dependency
ADD ./target/classes /usr/src/target/classes
ADD ./target/web /usr/src/target/web

# Update the entry point script
ENTRYPOINT java -Xmx2048m \
          -Drestolino.files=target/web \
          -Drestolino.classes=target/classes \
          -Drestolino.packageprefix=com.github.onsdigital.babbage.api \
          -javaagent:target/dependency/aws-opentelemetry-agent-1.31.0.jar \
          -Dotel.propagators=tracecontext,baggage \
          -Dotel.javaagent.enabled=false \
          -cp "target/dependency/*:target/classes/" \
          com.github.davidcarboni.restolino.Main
