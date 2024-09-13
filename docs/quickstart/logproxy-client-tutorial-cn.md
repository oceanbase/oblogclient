# LogProxy 客户端使用教程

[oblogproxy](https://github.com/oceanbase/oblogproxy) （OceanBase Log Proxy，以下称 LogProxy）是一个获取 OceanBase 增量 clog （commit log）的代理服务。本教程将向你展示如何使用 LogProxy 客户端连接 LogProxy ，并通过该连接获取日志数据。

## 准备

使用之前需要做的准备：

1. 安装 JDK 1.8 或更新的版本。
2. 启动 LogProxy 服务。
3. 如果 LogProxy 服务开启了 SSL 验证，需要准备好相关的证书文件。
4. 安装 Maven 或 Gradle，否则的话需要手动下载依赖的所有 jar 文件。

## 依赖配置

所有已发布的版本都可以在 [Maven 中央仓库](https://mvnrepository.com/artifact/com.oceanbase/oblogclient-logproxy)找到，你也可以选择从[归档目录](https://repo1.maven.org/maven2/com/oceanbase/oblogclient-logproxy/)手动下载。

如下所示是使用 Maven 时的示例：

```xml
<dependency>
  <groupId>com.oceanbase</groupId>
  <artifactId>oblogclient-logproxy</artifactId>
  <version>x.y.z</version>
</dependency>
```

如果你想要使用最新的快照版本，可以通过配置 Maven 快照仓库来指定：

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

## 使用

### 基本使用

连接 LogProxy 服务时，需要配置一些 `ObReaderConfig` 类的参数：

<div class="highlight">
    <table class="colwidths-auto docutils">
        <thead>
            <tr>
                <th class="text-left" style="width: 10%">参数名</th>
                <th class="text-left" style="width: 8%">是否必需</th>
                <th class="text-left" style="width: 7%">默认值</th>
                <th class="text-left" style="width: 10%">类型</th>
                <th class="text-left" style="width: 15%">设置函数</th>
                <th class="text-left" style="width: 50%">参数说明</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>cluster_url</td>
                <td>否</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setClusterUrl</td>
                <td>用于获取 OceanBase 集群节点信息的 url，当且仅当使用企业版的 OceanBase 时需要设置。使用 sys 用户执行 <code>show parameters like 'obconfig_url'</code> 时，返回的 value 字段即为该值。</td>
            </tr>
            <tr>
                <td>rootserver_list</td>
                <td>否</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setRsList</td>
                <td>OceanBase 集群的节点列表，当且仅当使用社区版的 OceanBase 时需要设置。使用 sys 用户执行 <code>show parameters like 'rootservice_list'</code> 时，返回的 value 字段即为该值。</td>
            </tr>
            <tr>
                <td>cluster_user</td>
                <td>是</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setUsername</td>
                <td>连接 OceanBase 的用户名，格式一般为 <code>用户名@租户名</code>。</td>
            </tr>
            <tr>
                <td>cluster_password</td>
                <td>是</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setPassword</td>
                <td>连接 OceanBase 的密码。</td>
            </tr>
            <tr>
                <td>tb_white_list</td>
                <td>否</td>
                <td style="word-wrap: break-word;">*.*.*</td>
                <td>String</td>
                <td>setTableWhiteList</td>
                <td>监听的数据变动白名单，使用 fnmatch 按照格式 <code>租户.库.表</code> 进行匹配，多个值使用 <code>|</code> 分隔。需要注意的是，当前使用的用户需要至少对监听的范围有 SELECT 权限。</td>
            </tr>
            <tr>
                <td>tb_black_list</td>
                <td>否</td>
                <td style="word-wrap: break-word;">｜</td>
                <td>String</td>
                <td>setTableBlackList</td>
                <td>监听的数据变动黑名单，使用 fnmatch 按照格式 <code>租户.库.表</code> 进行匹配，多个值使用 <code>|</code> 分隔。</td>
            </tr>
            <tr>
                <td>first_start_timestamp</td>
                <td>否</td>
                <td style="word-wrap: break-word;">0</td>
                <td>Long</td>
                <td>setStartTimestamp</td>
                <td>获取数据的起点时间戳，单位是秒。为 0 时将从当前时刻开始获取。</td>
            </tr>
            <tr>
                <td>timezone</td>
                <td>否</td>
                <td style="word-wrap: break-word;">+08:00</td>
                <td>String</td>
                <td>setTimezone</td>
                <td>连接使用的时区，该取值将会影响时间类型的字段读取。</td>
            </tr>
            <tr>
                <td>working_mode</td>
                <td>否</td>
                <td style="word-wrap: break-word;">storage</td>
                <td>String</td>
                <td>setWorkingMode</td>
                <td>libobcdc 的工作模式，可选值为 "storage" 和 "memory"。</td>
            </tr>
            <tr>
                <td>first_start_timestamp_us</td>
                <td>否</td>
                <td style="word-wrap: break-word;">0</td>
                <td>Long</td>
                <td>setStartTimestampUs</td>
                <td>订阅数据的起点时间戳，单位是微秒。为 0 时将从当前时刻开始。</td>
            </tr>
            <tr>
                <td>cluster_id</td>
                <td>否</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setClusterId</td>
                <td>OceanBase 集群的 cluster id。</td>
            </tr>
            <tr>
                <td>sys_user</td>
                <td>否</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setSysUsername</td>
                <td>设置 OceanBase 集群的 sys 租户用户的用户名。</td>
            </tr>
            <tr>
                <td>sys_password</td>
                <td>否</td>
                <td style="word-wrap: break-word;"></td>
                <td>String</td>
                <td>setSysPassword</td>
                <td>设置 OceanBase 集群的 sys 租户用户的密码。</td>
            </tr>
        </tbody>
    </table>
</div>

除了这些之外，还有其他参数可以通过 `ObReaderConfig` 类的构造函数进行配置，更多信息可以在[官方文档网站](https://www.oceanbase.com/docs/) 搜索 obcdc 获取。

下面是一个使用社区版 OceanBase 时配置 `ObReaderConfig` 的例子：

```java
ObReaderConfig config = new ObReaderConfig();
config.setRsList("127.0.0.1:2882:2881");
config.setUsername("user@tenant");
config.setPassword("password");
config.setStartTimestamp(0L);
config.setTableWhiteList("tenant.*.*");
```

如果使用企业版 OceanBase，可以参考如下配置：

```java
ObReaderConfig config = new ObReaderConfig();
config.setClusterUrl("http://127.0.0.1:8080/services?Action=ObRootServiceInfo&User_ID=alibaba&UID=ocpmaster&ObRegion=tenant");
config.setUsername("user@tenant");
config.setPassword("password");
config.setStartTimestamp(0L);
config.setTableWhiteList("tenant.*.*");
```

`ObReaderConfig` 配置完成后，就可以创建 LogProxy 客户端实例并开始监听数据变动了。

```java
LogProxyClient client = new LogProxyClient("127.0.0.1", 2983, config);

// 绑定一个处理日志数据的 RecordListener
client.addListener(new RecordListener() {

    @Override
    public void notify(LogMessage message){
        // 在此添加数据处理逻辑
    }

    @Override
    public void onException(LogProxyClientException e) {
        logger.error(e.getMessage());
    }
});

client.start();
client.join();
```

调用函数 `LogProxyClient.start()` 会启动一个新的线程，该线程将使用 Netty 建立一个到 LogProxy 的连接，并通过该连接接收日志数据。

LogProxy 客户端还可以通过 `ClientConf` 配置一些客户端行为相关的参数：

<div class="highlight">
    <table class="colwidths-auto docutils">
        <thead>
            <tr>
                <th class="text-left" style="width: 10%">参数名</th>
                <th class="text-left" style="width: 8%">是否必需</th>
                <th class="text-left" style="width: 7%">默认值</th>
                <th class="text-left" style="width: 10%">类型</th>
                <th class="text-left" style="width: 15%">设置函数</th>
                <th class="text-left" style="width: 50%">参数说明</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>clientId</td>
                <td>否</td>
                <td style="word-wrap: break-word;">系统生成</td>
                <td>String</td>
                <td>clientId</td>
                <td>用户自定义客户端id，默认由系统自动生成。</td>
            </tr>
            <tr>
                <td>transferQueueSize</td>
                <td>否</td>
                <td style="word-wrap: break-word;">20000</td>
                <td>Int</td>
                <td>transferQueueSize</td>
                <td>用于缓冲从LogProxy接收的记录的队列大小。</td>
            </tr>
            <tr>
                <td>connectTimeoutMs</td>
                <td>否</td>
                <td style="word-wrap: break-word;">5000</td>
                <td>Int</td>
                <td>connectTimeoutMs</td>
                <td>连接超时时间（以毫秒为单位）。</td>
            </tr>
            <tr>
                <td>readWaitTimeMs</td>
                <td>否</td>
                <td style="word-wrap: break-word;">2000</td>
                <td>Int</td>
                <td>readWaitTimeMs</td>
                <td>读取队列的超时时间（以毫秒为单位）。</td>
            </tr>
            <tr>
                <td>retryIntervalS</td>
                <td>否</td>
                <td style="word-wrap: break-word;">2</td>
                <td>Int</td>
                <td>retryIntervalS</td>
                <td>重试之间的间隔时间（以秒为单位）。</td>
            </tr>
            <tr>
                <td>idleTimeoutS</td>
                <td>否</td>
                <td style="word-wrap: break-word;">15</td>
                <td>Int</td>
                <td>idleTimeoutS</td>
                <td>Netty handler空闲超时时间（以秒为单位）。</td>
            </tr>
            <tr>
                <td>maxReconnectTimes</td>
                <td>否</td>
                <td style="word-wrap: break-word;">-1</td>
                <td>Int</td>
                <td>maxReconnectTimes</td>
                <td>断开连接后的最大重试次数，如果持续idleTimeoutS所配置的时间后，仍没有收到数据，则会触发重新连接。默认值-1代表无限重试。</td>
            </tr>
            <tr>
                <td>nettyDiscardAfterReads</td>
                <td>否</td>
                <td style="word-wrap: break-word;">16</td>
                <td>Int</td>
                <td>nettyDiscardAfterReads</td>
                <td>Netty尝试读取的最大次数，超过此次数的数据将被丢弃。</td>
            </tr>
            <tr>
                <td>ignoreUnknownRecordType</td>
                <td>否</td>
                <td style="word-wrap: break-word;">false</td>
                <td>boolean</td>
                <td>ignoreUnknownRecordType</td>
                <td>使用warning级别的日志记录未知或不受支持的消息类型，而不是抛出异常。</td>
            </tr>
            <tr>
                <td>protocolVersion</td>
                <td>否</td>
                <td style="word-wrap: break-word;">2</td>
                <td>Int</td>
                <td>protocolVersion</td>
                <td>与LogProxy服务（cdc模式）的通信协议版本。当前默认值即最新版本V2。</td>
            </tr>
            <tr>
                <td>sslContext</td>
                <td>否</td>
                <td style="word-wrap: break-word;"></td>
                <td>SslContext</td>
                <td>sslContext</td>
                <td>Netty ssl 上下文。</td>
            </tr>
        </tbody>
    </table>
</div>

如下所示是一个使用自定义 `ClientConf` 的例子：

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

客户端会将接收到的日志数据转为 `LogMessage`，更多信息可以参考 [LogMessage 介绍](../formats/logmessage-cn.md)。

完整的使用示例可以参考 [LogProxyClientTest.java](../../oblogclient-logproxy/src/test/java/com/oceanbase/clogproxy/client/LogProxyClientTest.java)。

### SSL 验证

如果 LogProxy 开启了 SSL 验证，在启动 LogProxy 客户端时将需要配置 [SslContext](https://netty.io/4.1/api/io/netty/handler/ssl/SslContext.html)：

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

### 版本兼容性

##### 组件ID

最初版本的 LogProxy 客户端使用 `'groupId'='com.oceanbase.logclient'`，`'artifactId'='logproxy-client'`，从 `1.1.0` 开始，更改组件 ID 为 `'groupId'='com.oceanbase'`，`'artifactId'='oblogclient-logproxy'`。如果你想使用旧版本的 LogProxy 客户端，可以在 Maven 中使用如下方式引入：

```xml
<dependency>
  <groupId>com.oceanbase.logclient</groupId>
  <artifactId>logproxy-client</artifactId>
  <version>1.0.7</version>
</dependency>
```

##### 黑白名单

从 4.x 版本开始，libobcdc 仅支持在租户级别监控非 sys 租户的 clog。 因此，对于OceanBase 4.x 版本的集群，白名单仅在租户级别生效，而黑名单则不再起作用。

##### 数据压缩

LogProxy 社区版从 1.0.1 开始默认会对发送到客户端的数据进行压缩，正确解压缩需要使用客户端 1.0.4 或之后的版本。

##### 使用 Client ID

LogProxy 使用 `ClientConf` 中的 `clientId` 来区分不同的连接，若想在并发环境下使用本客户端，或者想复用 clientId，需要使用客户端 1.0.4 或之后的版本。

## 问题排查

当 LogProxy 与客户端之间的连接建立成功后，LogProxy 将会开始向客户端发送日志数据，这里的日志数据主要有心跳和数据变动两类。也就是说，即使数据库在监听范围内没有变动，LogProxy 客户端也应当能收到心跳类型的数据。

如果 LogProxy 客户端启动后，没有报错信息出现，也没有收到任何数据，这时候为了确定问题出现的原因，需要查看 LogProxy 对应的 LogReader 子进程的状态，相关的信息在 LogProxy 部署目录的 `run/{clientId}/`。
