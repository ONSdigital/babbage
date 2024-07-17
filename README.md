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
| ENABLE_COVID19_FEATURE           |                        | Switch to use (or not) the covid feature                                                                          |
| HIGHCHARTS_EXPORT_SERVER         | http://localhost:9999/ | The URL to the highcharts export server                                                                           |
| IS_PUBLISHING                    | N                      | Switch to use (or not) the publishing functionality                                                               |
| MAP_RENDERER_HOST                | http://localhost:23500 | The URL to the map renderer                                                                                       |
| REDIRECT_SECRET                  | secret                 | The code for the redirect                                                                                         |
| TABLE_RENDERER_HOST              | http://localhost:23300 | The URL to the table renderer                                                                                     |
| POOLED_CONNECTION_TIMEOUT        | 5000                   | The number of milliseconds to wait before closing expired connections                                             |
| IDLE_CONNECTION_TIMEOUT          | 60                     | The number of seconds to wait before closing idle connections                                                     |
| OTEL_EXPORTER_OTLP_ENDPOINT      | http://localhost:4317  | URL for OpenTelemetry endpoint                                                                                    |
| OTEL_SERVICE_NAME                |                        | Service name to report to telemetry tools                                                                         |

### Debugging

When Babbage is run using one of its scripts (either run.sh or run-publishing.sh) it incorporates a Java Debug Wire Protocol (JDWP).

To create the configuration in Intellij, for calling the JDWP debugger, do as follows:

* Choose Run --> Edit Configurations...
* Click the + to create a new configuration
* For the type of configuration select either 'Remote' or 'Remote JVM' (whichever option it gives you)
* Give the new configuration a name e.g. Babbage Remote
* For 'Debugger mode' choose 'Attach to remote JVM'
* For 'Host' enter: localhost
* For 'Port' enter the port number given in the relevant script (e.g. for run-publishing.sh it's 8000)
* Intellij should automatically complete the command line arguments (note that these are similar to the jdwp arguments in the relevant script):

  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000

Then, to run and debug Babbage just do the following:

* At the command line, run the relevant script E.g.,
cd babbage
./run-publishing.sh

* Then in Intellij:
* Open babbage and add any breakpoints required
* Choose Run --> Debug 'Babbage Remote'

### Testing

To run unit tests:

`make test`

There is also a [guide for regression testing](REGRESSION.md). This is not complete and should not be seen as definitive.

### Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for details.

### License

Copyright © 2024, [Office for National Statistics](https://www.ons.gov.uk)

Released under MIT license, see [LICENSE](LICENSE.md) for details.
