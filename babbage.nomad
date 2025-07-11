job "babbage" {
  datacenters = ["eu-west-1"]
  region      = "eu"
  type        = "service"

  update {
    min_healthy_time = "30s"
    healthy_deadline = "5m"
    max_parallel     = 1
    auto_revert      = true
    stagger          = "150s"
  }

  group "web" {
    count = "{{WEB_TASK_COUNT}}"

    constraint {
      distinct_hosts = true
    }

    constraint {
      attribute = "${node.class}"
      value     = "web-mount"
    }

    restart {
      attempts = 3
      delay    = "15s"
      interval = "1m"
      mode     = "delay"
    }

    task "babbage-web" {
      driver = "docker"

      artifact {
        source = "s3::https://s3-eu-west-1.amazonaws.com/{{DEPLOYMENT_BUCKET}}/babbage/{{REVISION}}.tar.gz"
      }

      config {
        command = "${NOMAD_TASK_DIR}/start-task"

        args = [
          "java",
          "-server",
          "-Xms{{WEB_RESOURCE_HEAP_MEM}}m",
          "-Xmx{{WEB_RESOURCE_HEAP_MEM}}m",
          "-cp target/dependency/*:target/classes/",
          "-Drestolino.files=target/web",
          "-Drestolino.classes=target/classes",
          "-Drestolino.packageprefix=com.github.onsdigital.babbage.api",
          "-javaagent:target/dependency/aws-opentelemetry-agent-1.32.0.jar",
          "-Dotel.propagators=tracecontext,baggage",
          "-Dotel.javaagent.enabled=false",
          "-Dotel.exporter.otlp.protocol=grpc",
          "com.github.davidcarboni.restolino.Main",
        ]

        image = "{{ECR_URL}}:concourse-{{REVISION}}"

        port_map {
          http = 8080
        }
      }

      service {
        name = "babbage"
        port = "http"
        tags = ["web"]

        check {
          type     = "http"
          path     = "/health"
          interval = "10s"
          timeout  = "2s"
        }
      }

      resources {
        cpu    = "{{WEB_RESOURCE_CPU}}"
        memory = "{{WEB_RESOURCE_MEM}}"

        network {
          port "http" {}
        }
      }

      template {
        source      = "${NOMAD_TASK_DIR}/vars-template"
        destination = "${NOMAD_TASK_DIR}/vars"
      }

      vault {
        policies = ["babbage-web"]
      }
    }
  }

  group "publishing" {
    count = "{{PUBLISHING_TASK_COUNT}}"

    spread {
      attribute = "${node.unique.id}"
      weight    = 100
      # with `target` omitted, Nomad will spread allocations evenly across all values of the attribute.
    }
    spread {
      attribute = "${attr.platform.aws.placement.availability-zone}"
      weight    = 100
      # with `target` omitted, Nomad will spread allocations evenly across all values of the attribute.
    }

    constraint {
      attribute = "${node.class}"
      value     = "publishing"
    }

    restart {
      attempts = 3
      delay    = "15s"
      interval = "1m"
      mode     = "delay"
    }

    task "babbage-publishing" {
      driver = "docker"

      artifact {
        source = "s3::https://s3-eu-west-1.amazonaws.com/{{DEPLOYMENT_BUCKET}}/babbage/{{REVISION}}.tar.gz"
      }

      config {
        command = "${NOMAD_TASK_DIR}/start-task"

        args = [
          "java",
          "-server",
          "-Xms{{PUBLISHING_RESOURCE_HEAP_MEM}}m",
          "-Xmx{{PUBLISHING_RESOURCE_HEAP_MEM}}m",
          "-cp target/dependency/*:target/classes/",
          "-Drestolino.files=target/web",
          "-Drestolino.classes=target/classes",
          "-Drestolino.packageprefix=com.github.onsdigital.babbage.api",
          "-javaagent:target/dependency/aws-opentelemetry-agent-1.32.0.jar",
          "-Dotel.propagators=tracecontext,baggage",
          "-Dotel.javaagent.enabled=false",
          "-Dotel.exporter.otlp.protocol=grpc",
          "com.github.davidcarboni.restolino.Main",
        ]

        image = "{{ECR_URL}}:concourse-{{REVISION}}"

        port_map {
          http = 8080
        }
      }

      service {
        name = "babbage"
        port = "http"
        tags = ["publishing"]

        check {
          type     = "http"
          path     = "/health"
          interval = "10s"
          timeout  = "2s"
        }
      }

      resources {
        cpu    = "{{PUBLISHING_RESOURCE_CPU}}"
        memory = "{{PUBLISHING_RESOURCE_MEM}}"

        network {
          port "http" {}
        }
      }

      template {
        source      = "${NOMAD_TASK_DIR}/vars-template"
        destination = "${NOMAD_TASK_DIR}/vars"
      }

      vault {
        policies = ["babbage-publishing"]
      }
    }
  }
}
