OceanBase Log Client
---------------

OceanBase Log Client is a library for obtaining log of [OceanBase](https://github.com/oceanbase/oceanbase). There are modules as following:

- `common`: some common utils
- `logproxy-client`: the client for [oblogproxy](https://github.com/oceanbase/oblogproxy)

Communication
---------------
* [Official Q&A Website (Chinese)](https://open.oceanbase.com/answer) (Q&A, Ideas, General discussion)
* [GitHub Issues](https://github.com/oceanbase/oblogclient/issues) (Bug reports, feature requests)
* DingTalk Group (chat): 33254054

Binaries/Download
----------------
Binaries and dependency information for Maven, Ivy, Gradle and others can be found at http://search.maven.org.

Releases are available in the Maven Central repository. Take also a look at the [Releases](https://github.com/oceanbase/oblogclient/releases).

Example for Maven:

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

Usage
-----------

Basic usage

```java
ObReaderConfig config = new ObReaderConfig();
// Set root server list in format [ip1:rpc_port1:sql_port1;ip2:rpc_port2:sql_port2],
// while multiple servers are seperated by ';'.
config.setRsList("127.0.0.1:2882:2881");
// Set username and password.
config.setUsername("root@sys");
config.setPassword("root@sys");
// Set timestamp of start point in seconds, and zero means starting from now.
config.setStartTimestamp(0L);
// Set table whitelist in format [tenant.db.table], and '*' indicates any value.
config.setTableWhiteList("sys.*.*");

// Create a client instance.
LogProxyClient client = new LogProxyClient("127.0.0.1", 2983, config);

// Add a RecordListener to handle log messages.
client.addListener(new RecordListener() {

    @Override
    public void notify(LogMessage message){
        // process
    }

    @Override
    public void onException(LogProxyClientException e) {
        if (e.needStop()) {
            // handle error and stop client
            client.stop();
        }
    }
});

// Start and wait the Netty channel.
client.start();
client.join();
```

Use [SslContext](https://netty.io/4.1/api/io/netty/handler/ssl/SslContext.html) to encrypt communication between log client and log proxy.

```java
SslContext sslContext = SslContextBuilder.forClient()
        .sslProvider(SslContext.defaultClientProvider())
        .trustManager(this.getClass().getClassLoader().getResourceAsStream("server.crt"))
        .keyManager(this.getClass().getClassLoader().getResourceAsStream("client.crt"),
        this.getClass().getClassLoader().getResourceAsStream("client.key"))
        .build();
LogProxyClient client = new LogProxyClient("127.0.0.1", 2983, config, sslContext);
```

Once the client is successfully started, you should be able to receive `HEARTBEAT` and other messages in the notify method.

License
-------
Mulan Permissive Software License, Version 2 (Mulan PSL v2). See the [LICENSE](LICENCE) file for details.
