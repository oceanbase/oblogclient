# LogProxyClient Tutorial

[oblogproxy](https://github.com/oceanbase/oblogproxy) (OceanBase Log Proxy, hereinafter LogProxy) is a proxy service which can fetch incremental log data from OceanBase. This tutorial will show you how to use LogProxyClient to connect to LogProxy and get the log data.

## Preparation

There are some requirements to use LogProxyClient:

1. JDK 1.8 or higher version installed.
2. LogProxy started.
3. SSL certificate files if SSL encryption is enabled.
4. Maven or Gradle installed, otherwise you need download the jar file manually.


## Binaries/Download

Releases are available in the [Maven Central](https://mvnrepository.com/artifact/com.oceanbase.logclient/logproxy-client), you can also download the jar file manually from the [archive](https://repo1.maven.org/maven2/com/oceanbase/logclient/logproxy-client/).

Here is an example for Maven:

```xml
<dependency>
  <groupId>com.oceanbase.logclient</groupId>
  <artifactId>logproxy-client</artifactId>
  <version>x.y.z</version>
</dependency>
```

If you'd rather like the latest snapshots of the upcoming major version, use our Maven snapshot repository and declare the appropriate dependency version.

```xml
<dependency>
  <groupId>com.oceanbase.logclient</groupId>
  <artifactId>logproxy-client</artifactId>
  <version>x.y.z-SNAPSHOT</version>
</dependency>

<repositories>
  <repository>
    <id>sonatype-snapshots</id>
    <name>Sonatype Snapshot Repository</name>
    <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

## Usage

### Basic Usage

To connect to LogProxy, there are some parameters to set in `ObReaderConfig`:

- *cluster_url*: Cluster config url used to set up the OBConfig service. Required for OceanBase Enterprise Edition.
- *rootserver_list*: Root server list of OceanBase cluster in format `ip1:rpc_port1:sql_port1;ip2:rpc_port2:sql_port2`, IP address here must be able to be resolved by LogProxy. Required for OceanBase Community Edition.
- *cluster_username*: Username for OceanBase, the format is `username@tenant_name#cluster_name` when connecting to [obproxy](https://github.com/oceanbase/obproxy) or `username@tenant_name` when directly connecting to OceanBase server.
- *cluster_password*: Password for OceanBase when using configured `cluster_username`.
- *first_start_timestamp*: Start timestamp in seconds, and zero means starting from now. Default is `0`.
- *tb_white_list*: Table whitelist in format `tenant_name.database_name.table_name`, `*` indicates any value, and multiple values can be separated by `|`. Default is `*.*.*`.
- *tb_black_list*: Table blacklist in the same format with whitelist. Default is `|`.
- *timezone*: Timezone offset from UTC. Default value is `+8:00`.
- *working_mode*: Working mode. Can be `storage` (default) or `memory`.

These parameters are used in `obcdc` (former `liboblog`), and the items not listed above can be passed to `obcdc` through the `ObReaderConfig` constructor with parameters.

Here is an example to set `ObReaderConfig` with OceanBase Community Edition:

```java
ObReaderConfig config = new ObReaderConfig();
config.setRsList("127.0.0.1:2882:2881");
config.setUsername("username");
config.setPassword("password");
config.setStartTimestamp(0L);
config.setTableWhiteList("tenant.*.*");
```

If you want to work with OceanBase Enterprise Edition, you can set the `ObReaderConfig` with `cluster_url` like below:

```java
ObReaderConfig config = new ObReaderConfig();
config.setClusterUrl("http://127.0.0.1:8080/services?Action=ObRootServiceInfo&User_ID=alibaba&UID=ocpmaster&ObRegion=tenant");
config.setUsername("username");
config.setPassword("password");
config.setStartTimestamp(0L);
config.setTableWhiteList("tenant.*.*");
```

Once ObReaderConfig is set properly, you can use it to instance a LogProxyClient and monitor the log data.

```java
LogProxyClient client = new LogProxyClient("127.0.0.1", 2983, config);

// Add a RecordListener to handle log messages.
client.addListener(new RecordListener() {

    @Override
    public void notify(LogMessage message){
        // add process here
    }

    @Override
    public void onException(LogProxyClientException e) {
        logger.error(e.getMessage());
    }
});

client.start();
client.join();
```

The method `LogProxyClient.start()` will start a new thread which serving with a netty socket to receive data from LogProxy.

There are also some configurations for the client in `ClientConf`, if you don't want to use its default values, you can customize a `ClientConf` and pass it to the corresponding constructor to create the client instance.

```java
ClientConf clientConf =
        ClientConf.builder()
                .clientId("myClientId")
                .transferQueueSize(1024)
                .connectTimeoutMs(1000)
                .readWaitTimeMs(1000)
                .retryIntervalS(1)
                .maxReconnectTimes(10)
                .idleTimeoutS(10)
                .nettyDiscardAfterReads(1)
                .ignoreUnknownRecordType(true)
                .build();
LogProxyClient client = new LogProxyClient("127.0.0.1", 2983, config, clientConf);
```

The received log records are parsed to `LogMessage` in the client handler, you can see [LogMessage doc](../formats/logmessage.md) for more details.

### SSL Encryption

If SSL verification is enabled at LogProxy, you should instance a LogProxyClient with [SslContext](https://netty.io/4.1/api/io/netty/handler/ssl/SslContext.html). For example:

```java
SslContext sslContext = SslContextBuilder.forClient()
        .sslProvider(SslContext.defaultClientProvider())
        .trustManager(this.getClass().getClassLoader().getResourceAsStream("ca.crt"))
        .keyManager(
            this.getClass().getClassLoader().getResourceAsStream("client.crt"),
            this.getClass().getClassLoader().getResourceAsStream("client.key"))
        .build();
ClientConf clientConf = ClientConf.builder().sslContext(sslContext).build();
LogProxyClient client = new LogProxyClient("127.0.0.1", 2983, config, clientConf);
```

Here you need provide following files:
- *ca.crt*: Trusted certificate for verifying the remote endpoint's certificate, should be same with LogProxy.
- *client.crt*: Certificate of this client.
- *client.key*: Private key of this client.

See [manual](https://github.com/oceanbase/oblogproxy/blob/master/docs/manual.md) of LogProxy for more details about SSL encryption.

## Version Compatibility

The communication protocol between the `logproxy-client` and `oblogproxy` is forward compatible, and the latest version of `logproxy-client` can work with any version of `oblogproxy`. But for legacy versions, there are some restrictions in functionality.

#### OceanBase Enterprise Edition

To monitor change data from OceanBase EE, you need to configure `cluster_url` to replace the `rootserver_list` parameter for `obcdc`, which is supported from `1.0.4` of the client.

#### Record Compression

The log proxy compresses the record data by default from `1.0.1`, and the client fixed the bug in decompression process with [#33](https://github.com/oceanbase/oblogclient/pull/33) from `1.0.4`. So if you want to work with log proxy `1.0.1` or later version, you should use `1.0.4` or later version of the client.

#### Reuse Client Id

The log proxy use `clientId` to identify a connection, and reuse it will make the log proxy reduce the use of hardware resources. In legacy versions of the client, there is a bug [#38](https://github.com/oceanbase/oblogclient/issues/38) which will cause connection close failure, and it's fixed in `1.0.4`. So you can only reuse a fixed `clientId` from `1.0.4` of the client.

## Heartbeat and Troubleshooting

Once the connection is established properly, LogProxy will start to fetch log messages from OceanBase and send them to LogProxyClient. When the connection is idle, LogProxy will send heartbeat messages to LogProxyClient.

Note that when LogProxy receives the ObReaderConfig, it will only check the format but won't verify the content. So only when the LogProxyClient receives log messages or heartbeat messages can we be sure that the connection is established properly. If the connection doesn't work properly and there is no error message in log of LogProxyClient, you need to check the log of `oblogreader` at LogProxy side, which is under `$(logproxy_home)/run` by default.
