# Babbage

Repository for ONS Website Babbage

Babbage is a bespoke service for translating JSON into HTML. The Zebedee-Reader reads .json files off the server for passing to Babbage. Babbage then translates them into the .html files that are used in the website.

Babbage contains two main areas of functionality, as follows:

1. It creates the HTML files for the pages on the website.
2. It creates the HTML files for the website publications in the publishing system [Florence](https://github.com/ONSdigital/florence)

## Getting started

In the babbage repo do one of the following:

* To run Babbage for the website functionality use this command

```shell script
./run.sh
```

* To run Babbage for the publishing functionality use this command

```shell script
./run-publishing.sh
```

### Dependencies

Babbage runs independently. However, in order to run it locally in its publishing mode, with the other required services for that, there is a stack that can be used in dp-compose:

[Homepage publishing](https://github.com/ONSdigital/dp-compose/tree/main/v2/stacks#homepage-publishing)

### Configuration

| Environment variable             | Default                | Description                                                                                                       |
|----------------------------------|------------------------|-------------------------------------------------------------------------------------------------------------------|
| CONTENT_SERVICE_MAX_CONNECTION   | 50                     | The maximum number of connections Babbage can make to the content service                                         |
| CONTENT_SERVICE_URL              | http://localhost:8082  | The URL to the content service (zebedee)                                                                          |
| ELASTIC_SEARCH_SERVER            | localhost              | The elastic search host and port (The http:// scheme prefix is added programmatically)                            |
| ELASTIC_SEARCH_CLUSTER           |                        | The elastic search cluster                                                                                        |
| ENABLE_CACHE                     | N                      | Switch to use (or not) the cache                                                                                  |
| ENABLE_COVID19_FEATURE           |                        | Switch to use (or not) the covid feature                                                                          |
| ENABLE_METRICS                   | N                      | Switch to collect (or not) metrics about cache expiry times                                                       |
| METRICS_FORMAT                   | Text                   | Available options are Text or Open documented here <https://prometheus.io/docs/instrumenting/exposition_formats/> |
| HIGHCHARTS_EXPORT_SERVER         | http://localhost:9999/ | The URL to the highcharts export server                                                                           |
| IS_PUBLISHING                    | N                      | Switch to use (or not) the publishing functionality                                                               |
| MAP_RENDERER_HOST                | http://localhost:23500 | The URL to the map renderer                                                                                       |
| REDIRECT_SECRET                  | secret                 | The code for the redirect                                                                                         |
| TABLE_RENDERER_HOST              | http://localhost:23300 | The URL to the table renderer                                                                                     |
| POOLED_CONNECTION_TIMEOUT        | 5000                   | The number of milliseconds to wait before closing expired connections                                             |
| IDLE_CONNECTION_TIMEOUT          | 60                     | The number of seconds to wait before closing idle connections                                                     |
| POST_PUBLISH_CACHE_MAX_AGE       | 10                     | The cache max age for the post publish window                                                                     |
| POST_PUBLISH_CACHE_EXPIRY_OFFSET | 180                    | The length of the post publish window                                                                             |
| OTEL_EXPORTER_OTLP_ENDPOINT      | http://localhost:4317  | URL for OpenTelemetry endpoint                                                                                    |
| OTEL_SERVICE_NAME                |                        | Service name to report to telemetry tools                                                                         |

### Metrics

To see the metrics, you need to set the ENABLE_METRICS and ENABLE_CACHE environment variables (default is N) when babbage starts up. Then call the following command while babbage is running:

```bash
curl -s http://localhost:8080/metrics
```

The metrics should look something like this:

```shell
# HELP publish_date_too_far_in_past_total Total requests for uris that have a past publishing date too long ago (outside a given time span)
# TYPE publish_date_too_far_in_past_total counter
publish_date_too_far_in_past_total 0.0
# HELP publish_date_too_far_in_future_total Total requests for uris that have a future publishing date later than that calculated by the default expiry time
# TYPE publish_date_too_far_in_future_total counter
publish_date_too_far_in_future_total 0.0
# HELP publish_date_not_present_total Total requests for uris that have no publishing date found
# TYPE publish_date_not_present_total counter
publish_date_not_present_total 0.0
# HELP publish_date_in_range_total Total requests for uris that have a publishing date within the range required for setting the cache expiry time
# TYPE publish_date_in_range_total counter
publish_date_in_range_total 0.0
# HELP publish_date_in_range_created Total requests for uris that have a publishing date within the range required for setting the cache expiry time
# TYPE publish_date_in_range_created gauge
publish_date_in_range_created 1.686667997375E9
# HELP publish_date_not_present_created Total requests for uris that have no publishing date found
# TYPE publish_date_not_present_created gauge
publish_date_not_present_created 1.686667997376E9
# HELP publish_date_too_far_in_future_created Total requests for uris that have a future publishing date later than that calculated by the default expiry time
# TYPE publish_date_too_far_in_future_created gauge
publish_date_too_far_in_future_created 1.686667997376E9
# HELP publish_date_too_far_in_past_created Total requests for uris that have a past publishing date too long ago (outside a given time span)
# TYPE publish_date_too_far_in_past_created gauge
publish_date_too_far_in_past_created 1.686667997376E9
```

### Debugging

When Babbage is run using one of its scripts (either run.sh or run-publishing.sh) it incorporates a Java Debug Wire Protocol (JDWP).

To create the configuration in Intellij, for calling the JDWP debugger, do as follows:

- Choose Run --> Edit Configurations...
- Click the + to create a new configuration
- For the type of configuration select either 'Remote' or 'Remote JVM' (whichever option it gives you)
- Give the new configuration a name e.g. Babbage Remote
- For 'Debugger mode' choose 'Attach to remote JVM'
- For 'Host' enter: localhost
- For 'Port' enter the port number given in the relevant script (e.g. for run-publishing.sh it's 8000)
- Intellij should automatically complete the command line arguments (note that these are similar to the jdwp arguments in the relevant script):

  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000

Then, to run and debug Babbage just do the following:

- At the command line, run the relevant script E.g.,
cd babbage
./run-publishing.sh

- Then in Intellij:
- Open babbage and add any breakpoints required
- Choose Run --> Debug 'Babbage Remote'

### Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for details.
