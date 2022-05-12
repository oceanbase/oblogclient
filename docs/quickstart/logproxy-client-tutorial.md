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

## Workflow

![image](../images/logproxy-client-workflow.png)

When `LogProxyClient.start()` is executed, a new thread will be created in `ClientStream`. The thread will initialize a netty channel which will receive log data from LogProxy and put the data as `TransferPacket` to a BlockingQueue. When the netty connection is established, the thread will poll the queue and pass the `LogMessage` in TransferPacket to `RecordListener.notify`.

## Usage

### Basic Usage

To connect to LogProxy, there are some parameters to set in `ObReaderConfig`:

- *cluster_url*: Cluster config url used to set up the OBConfig service. Required for OceanBase Enterprise Edition.
- *rootserver_list*: Root server list of OceanBase cluster in format `ip1:rpc_port1:sql_port1;ip2:rpc_port2:sql_port2`, IP address here must be able to be resolved by LogProxy. Required for OceanBase Community Edition.
- *cluster_username*: Username for OceanBase, the format is `username@tenant_name#cluster_name` when connecting to [obproxy](https://github.com/oceanbase/obproxy) or `username@tenant_name` when directly connecting to OceanBase server.
- *cluster_password*: Password for OceanBase when using configured `cluster_username`.
- *first_start_timestamp*: Start timestamp in seconds, and zero means starting from now.
- *tb_white_list*: Table whitelist in format `tenant_name.database_name.table_name`, `*` indicates any value, and multiple values can be separated by `|`.

These parameters are used in `liboblog`, you can check the [doc](https://github.com/oceanbase/oceanbase-doc/blob/V3.1.2/zh-CN/9.supporting-tools/4.cdc/2.liboblog/2.liboblog-parameters/2.liboblog-configuration-items.md) for more details.

Here is an example to set ObReaderConfig with a user of sys tenant, and the OceanBase and LogProxy server are on the same machine.

```java
ObReaderConfig config = new ObReaderConfig();
config.setRsList("127.0.0.1:2882:2881");
config.setUsername("user@sys");
config.setPassword("pswd");
config.setStartTimestamp(0L);
config.setTableWhiteList("sys.db1.tb1|sys.db2.*");
```

Once ObReaderConfig is set properly, you can use it to instance a LogProxyClient and listen to the log data.

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
        if (e.needStop()) {
            // add error hander here
            client.stop();
        }
    }
});

client.start();
client.join();
```

The method `LogProxyClient.start()` will start a new thread which serving with a netty socket to receive data from LogProxy.

For details about `LogMessage`, see [LogMessage](../formats/logmessage.md).

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
LogProxyClient client = new LogProxyClient("127.0.0.1", 2983, config, sslContext);
```

Here you need provide following files:
- *ca.crt*: Trusted certificate for verifying the remote endpoint's certificate, should be same with LogProxy.
- *client.crt*: Certificate of this client.
- *client.key*: Private key of this client.

See [manual](https://github.com/oceanbase/oblogproxy/blob/master/docs/manual.md) of LogProxy for more details about SSL encryption.

## Heartbeat and Troubleshooting

Once the connection is established properly, LogProxy will start to fetch log messages from OceanBase and send them to LogProxyClient. When the connection is idle, LogProxy will send heartbeat messages to LogProxyClient.

Note that when LogProxy receives the ObReaderConfig, it will only check the format but won't verify the content. So only when the LogProxyClient receives log messages or heartbeat messages can we be sure that the connection is established properly. If the connection doesn't work properly and there is no error message in log of LogProxyClient, you need to check the log of `oblogreader` at LogProxy side, which is under `$(logproxy_home)/run` by default.
