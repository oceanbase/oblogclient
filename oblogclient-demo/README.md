OceanBase Log Client Demo
-------------------------

English | [简体中文](README_CN.md)

This is a demo for oblogclient. You can use it to subscribe to the log data of your OceanBase database by modifying the
configuration file `application.properties`.

| Property                     | Description                                                                                                                                                                                |
|------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| username                     | The username of the non-sys tenant that needs to be subscribed                                                                                                                             |
| password                     | The password                                                                                                                                                                               |
| logproxy.host                | IP or hostname of logproxy service                                                                                                                                                         |
| logproxy.port                | The port of the logproxy service                                                                                                                                                           |
| logproxy.client_id           | The client id used by the client in the demo. If it is empty, the application will automatically generate it by IP address and timestamp                                                   |
| logproxy.connect_timeout     | logproxy client connection timeout                                                                                                                                                         |
| logproxy.max_reconnect_times | The maximum number of retries for logproxy client connections                                                                                                                              |
| libobcdc.rootservice_list    | Required by OceanBase CE, which can be queried through `show parameters like 'rootservice_list'`                                                                                           |
| libobcdc.obconfig_url        | Required by OceanBase EE, which can be queried through `show parameters like 'obconfig_url'`                                                                                               |
| libobcdc.tb_white_list       | Subscription whitelist, the format is `tenant.db.table`, and `*` is a wildcard character                                                                                                   |
| libobcdc.start_timestamp     | Subscription start time, 0 means starting from the current time                                                                                                                            |
| libobcdc.timezone            | Time zone, only affects time type reading in mysql mode                                                                                                                                    |
| libobcdc.working_mode        | Working mode, which can be 'memory' (processing in memory, which is fast) or 'storage' (temporarily save the log data of one transaction to disk, which can work with large transactions). |

After the configuration file is modified, you can use the IDE for debugging and running.

You can also modify the configuration file, place it in the directory where the jar package is located, and then run it
through the following command:

```shell
java -jar oblogclient-0.0.1-SNAPSHOT-jar-with-dependencies.jar application.properties
```
