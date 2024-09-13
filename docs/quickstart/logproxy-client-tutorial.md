# LogProxyClient Tutorial

[oblogproxy](https://github.com/oceanbase/oblogproxy) (OceanBase Log Proxy, hereinafter LogProxy) is a proxy service which can fetch the clog (commit log) data from OceanBase. This tutorial will show you how to use LogProxy client to connect to LogProxy and get the log data.

## Preparation

There are some requirements:

1. JDK 1.8 or higher version installed.
2. LogProxy started.
3. SSL certificate files if SSL encryption is enabled.
4. Maven or Gradle installed, otherwise you need download the jar files manually.

## Binaries/Download

Releases are available in the [Maven Central](https://mvnrepository.com/artifact/com.oceanbase/oblogclient-logproxy), you can also download the jar file manually from the [archive](https://repo1.maven.org/maven2/com/oceanbase/oblogclient-logproxy/).

Here is an example for Maven:

```xml
<dependency>
  <groupId>com.oceanbase</groupId>
  <artifactId>oblogclient-logproxy</artifactId>
  <version>x.y.z</version>
</dependency>
```

If you'd rather like the latest snapshots of the upcoming major version, use our Maven snapshot repository and declare the appropriate dependency version.

```xml
<dependency>
  <groupId>com.oceanbase</groupId>
  <artifactId>oblogclient-logproxy</artifactId>
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

To connect to LogProxy, there are some options in `ObReaderConfig`:

<div class="highlight">
    <table class="colwidths-auto docutils">
        <thead>
            <tr>
                <th class="text-left" style="width: 10%">Option</th>
                <th class="text-left" style="width: 8%">Required</th>
                <th class="text-left" style="width: 7%">Default</th>
                <th class="text-left" style="width: 10%">Type</th>
                <th class="text-left" style="width: 15%">Setter</th>
                <th class="text-left" style="width: 50%">Description</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>cluster_url</td>
                <td>false</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setClusterUrl</td>
                <td>The url used to get information about servers of OceanBase Enterprise Edition. Query with <code>show parameters like 'obconfig_url'</code> using user of `sys` tenant, and you can get it at the `value` field.</td>
            </tr>
            <tr>
                <td>rootserver_list</td>
                <td>false</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setRsList</td>
                <td>The server list of OceanBase Community Edition. Query with <code>show parameters like 'rootservice_list'</code> using user of `sys` tenant, and you can get it at the `value` field.</td>
            </tr>
            <tr>
                <td>cluster_user</td>
                <td>true</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setUsername</td>
                <td>Username of OceanBase.</td>
            </tr>
            <tr>
                <td>cluster_password</td>
                <td>true</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setPassword</td>
                <td>Password of OceanBase.</td>
            </tr>
            <tr>
                <td>tb_white_list</td>
                <td>false</td>
                <td style="word-wrap: break-word;">*.*.*</td>
                <td>String</td>
                <td>setTableWhiteList</td>
                <td>Table whitelist in format <code>tenant_name.database_name.table_name</code>. Pattern matching is provided by fnmatch, and multiple values can be separated by <code>|</code>. Note that the user should have at least the SELECT privilege on this whitelist.</td>
            </tr>
            <tr>
                <td>tb_black_list</td>
                <td>false</td>
                <td style="word-wrap: break-word;">｜</td>
                <td>String</td>
                <td>setTableBlackList</td>
                <td>Table blacklist in format <code>tenant_name.database_name.table_name</code>. Pattern matching is provided by fnmatch, and multiple values can be separated by <code>|</code>. </td>
            </tr>
            <tr>
                <td>first_start_timestamp</td>
                <td>false</td>
                <td style="word-wrap: break-word;">0</td>
                <td>Long</td>
                <td>setStartTimestamp</td>
                <td>Timestamp of the starting point of data in seconds, and zero means starting from now.</td>
            </tr>
            <tr>
                <td>timezone</td>
                <td>false</td>
                <td style="word-wrap: break-word;">+08:00</td>
                <td>String</td>
                <td>setTimezone</td>
                <td>Timezone used to convert data of temporal types.</td>
            </tr>
            <tr>
                <td>working_mode</td>
                <td>false</td>
                <td style="word-wrap: break-word;">storage</td>
                <td>String</td>
                <td>setWorkingMode</td>
                <td>Working mode of libobcdc, can be 'storage' or 'memory'.</td>
            </tr>
            <tr>
                <td>first_start_timestamp_us</td>
                <td>false</td>
                <td style="word-wrap: break-word;">0</td>
                <td>Long</td>
                <td>setStartTimestampUs</td>
                <td>Timestamp of the starting point of data in microseconds, and zero means starting from now.</td>
            </tr>
            <tr>
                <td>cluster_id</td>
                <td>false</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setClusterId</td>
                <td>The cluster id of OceanBase.</td>
            </tr>
            <tr>
                <td>sys_user</td>
                <td>false</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setSysUsername</td>
                <td>The username of OceanBase sys tenant user.</td>
            </tr>
            <tr>
                <td>sys_password</td>
                <td>false</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setSysPassword</td>
                <td>The password of OceanBase sys tenant user.</td>
            </tr>
        </tbody>
    </table>
</div>

There are some other options that can be set with the constructor of `ObReaderConfig`, you can search obcdc on the [doc website](https://www.oceanbase.com/docs/) for more details.

Here is an example to set `ObReaderConfig` with OceanBase Community Edition:

```java
ObReaderConfig config = new ObReaderConfig();
config.setRsList("127.0.0.1:2882:2881");
config.setUsername("user@tenant");
config.setPassword("password");
config.setStartTimestamp(0L);
config.setTableWhiteList("tenant.*.*");
```

And you can set `ObReaderConfig` for OceanBase Enterprise Edition like below:

```java
ObReaderConfig config = new ObReaderConfig();
config.setClusterUrl("http://127.0.0.1:8080/services?Action=ObRootServiceInfo&User_ID=alibaba&UID=ocpmaster&ObRegion=tenant");
config.setUsername("user@tenant");
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

There are also some configurations for the client in `ClientConf`:

<div class="highlight">
    <table class="colwidths-auto docutils">
        <thead>
            <tr>
                <th class="text-left" style="width: 10%">Option</th>
                <th class="text-left" style="width: 8%">Required</th>
                <th class="text-left" style="width: 7%">Default</th>
                <th class="text-left" style="width: 10%">Type</th>
                <th class="text-left" style="width: 15%">Setter</th>
                <th class="text-left" style="width: 50%">Description</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>clientId</td>
                <td>false</td>
                <td style="word-wrap: break-word;">system-generated</td>
                <td>String</td>
                <td>clientId</td>
                <td>User-defined client id, automatically generated by the system by default.</td>
            </tr>
            <tr>
                <td>transferQueueSize</td>
                <td>false</td>
                <td style="word-wrap: break-word;">20000</td>
                <td>Int</td>
                <td>transferQueueSize</td>
                <td>Queue size for storing records received from log proxy.</td>
            </tr>
            <tr>
                <td>connectTimeoutMs</td>
                <td>false</td>
                <td style="word-wrap: break-word;">5000</td>
                <td>Int</td>
                <td>connectTimeoutMs</td>
                <td>Connection timeout in milliseconds.</td>
            </tr>
            <tr>
                <td>readWaitTimeMs</td>
                <td>false</td>
                <td style="word-wrap: break-word;">2000</td>
                <td>Int</td>
                <td>readWaitTimeMs</td>
                <td>Reading queue timeout in milliseconds.</td>
            </tr>
            <tr>
                <td>retryIntervalS</td>
                <td>false</td>
                <td style="word-wrap: break-word;">2</td>
                <td>Int</td>
                <td>retryIntervalS</td>
                <td>Time to sleep in seconds when retrying.</td>
            </tr>
            <tr>
                <td>idleTimeoutS</td>
                <td>false</td>
                <td style="word-wrap: break-word;">15</td>
                <td>Int</td>
                <td>idleTimeoutS</td>
                <td>Idle timeout in seconds for netty handler.</td>
            </tr>
            <tr>
                <td>maxReconnectTimes</td>
                <td>false</td>
                <td style="word-wrap: break-word;">-1</td>
                <td>Int</td>
                <td>maxReconnectTimes</td>
                <td>Maximum number of retries after disconnect, if not data income lasting idleTimeoutS, a reconnection will be triggered. The default value -1 means infinite retries</td>
            </tr>
            <tr>
                <td>nettyDiscardAfterReads</td>
                <td>false</td>
                <td style="word-wrap: break-word;">16</td>
                <td>Int</td>
                <td>nettyDiscardAfterReads</td>
                <td>Maximum number of reads, after which data will be discarded.</td>
            </tr>
            <tr>
                <td>ignoreUnknownRecordType</td>
                <td>false</td>
                <td style="word-wrap: break-word;">false</td>
                <td>boolean</td>
                <td>ignoreUnknownRecordType</td>
                <td>Ignore unknown or unsupported record type with a warning log instead of throwing an exception.</td>
            </tr>
            <tr>
                <td>protocolVersion</td>
                <td>false</td>
                <td style="word-wrap: break-word;">2</td>
                <td>Int</td>
                <td>protocolVersion</td>
                <td>Communication protocol version with the LogProxy service (cdc mode). The current default value is the latest version V2.</td>
            </tr>
            <tr>
                <td>sslContext</td>
                <td>false</td>
                <td style="word-wrap: break-word;"></td>
                <td>SslContext</td>
                <td>sslContext</td>
                <td>Netty ssl context.</td>
            </tr>
        </tbody>
    </table>
</div>

If you don't want to use its default values, you can customize a `ClientConf` and pass it to the corresponding constructor to create the client instance.

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

To get a full example, you can check the [LogProxyClientTest.java](../../oblogclient-logproxy/src/test/java/com/oceanbase/clogproxy/client/LogProxyClientTest.java).

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

### Version Compatibility

#### GroupId and ArtifactId

The initial version of LogProxy client is released with `'groupId'='com.oceanbase.logclient'` and `'artifactId'='logproxy-client'`, and we use `'groupId'='com.oceanbase'` and `'artifactId'='oblogclient-logproxy'` since `1.1.0`. If you want to use a legacy version of LogProxy client, you can add it to Maven as following:

```xml
<dependency>
  <groupId>com.oceanbase.logclient</groupId>
  <artifactId>logproxy-client</artifactId>
  <version>1.0.7</version>
</dependency>
```

##### Whitelist and Blacklist

Starting from version 4.x, libobcdc only supports to monitor the commit log of a non-sys tenant at the tenant level. Therefore, for OceanBase clusters in version 4.x, the whitelist only takes effect up to the tenant level, and blacklist does not work anymore.

#### Record Compression

The log proxy compresses the record data by default from `1.0.1`, and to decompress the data properly, you should use `1.0.4` or later version of the client.

#### Use Client Id

The LogProxy use `clientId` to identify a connection, if you want to use the client concurrently, or you want to reuse the `clientId`, you should use `1.0.4` or later version of the client.

## Troubleshooting

When the connection between LogProxy and the client is successfully established, LogProxy will send log data to the client. The log data here mainly includes heartbeat and data changes. Even if the database does not change within the monitoring scope, the LogProxy client should be able to receive heartbeat data.

If the LogProxy client does not receive data and no error message appears after startup, in order to determine the cause of the issue, you should check the LogReader thread of the LogProxy for this connection, and the working space of LogReader is `run/ {clientId}/`.
