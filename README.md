OceanBase Log Client
---------------

OceanBase Log Client is a Java client for [oblogproxy](https://github.com/oceanbase/oblogproxy). It uses [netty](https://github.com/netty/netty) to connect to the log proxy server and receive the incremental change log in real time.

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
    <groupId>com.oceanbase.logproxy.client</groupId>
    <artifactId>client</artifactId>
    <version>x.y.z</version>
</dependency>
```

If you'd rather like the latest snapshots of the upcoming major version, use our Maven snapshot repository and declare the appropriate dependency version.

```xml
<dependency>
    <groupId>com.oceanbase.logproxy.client</groupId>
    <artifactId>client</artifactId>
    <version>x.y.z-SNAPSHOT</version>
</dependency>

<repositories>
  <repository>
    <id>sonatype-snapshots</id>
    <name>Sonatype Snapshot Repository</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
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
// set root server list in format [ip:rpc_port:sql_port]
config.setRsList("127.0.0.1:2882:2881");
// username and password
config.setUsername("root@sys");
config.setPassword("root@sys");
// timestamp of start point, zero means starting from now
config.setStartTimestamp(0L);
// whitelist in format [tenant.db.table]
config.setTableWhiteList("sys.*.*");

// create a client
LogProxyClient client = new LogProxyClient("127.0.0.1", 2983, config);

// add handler
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

// start and wait
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
