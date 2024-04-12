OceanBase 日志客户端示例
--------------------

[English](README.md) | 简体中文

这是一个使用日志客户端的示例程序，你可以通过修改配置文件 `application.properties` 来使用它订阅自己的 OceanBase 数据库的日志。

| 配置项                          | 说明                                                            |
|------------------------------|---------------------------------------------------------------|
| username                     | 需要订阅的业务租户的用户名                                                 |
| password                     | 订阅用户的密码                                                       |
| logproxy.host                | logproxy 服务的 ip 或域名                                           |
| logproxy.port                | logproxy 服务的端口                                                |
| logproxy.client_id           | 示例中客户端使用的 client id，为空时程序将自动生成                                |
| logproxy.connect_timeout     | logproxy 客户端连接超时时间                                            |
| logproxy.max_reconnect_times | logproxy 客户端连接的最大重试次数                                         |
| libobcdc.rootservice_list    | 社区版集群参数，可通过 `show parameters like 'rootservice_list'` 获得      |
| libobcdc.obconfig_url        | 企业版集群参数，可通过 `show parameters like 'obconfig_url'` 获得          |
| libobcdc.tb_white_list       | 订阅白名单，格式为 `租户名.库名.表名`，星号为通配符                                  |
| libobcdc.start_timestamp     | 订阅起始时间，0表示从当前时间开始                                             |
| libobcdc.timezone            | 时区，仅影响 mysql 模式下的时间类型读取                                       |
| libobcdc.working_mode        | 工作模式，可以是 'memory'（全程内存处理，速度快） 或 'storage'（单事务日志会临时落盘，可以支持大事务） |

配置文件修改完成后，即可借助 IDE 进行调试运行。

您也可以在修改完配置文件之后，将其放置在 jar 包所在目录下，然后通过如下命令运行示例程序：

```shell
java -jar oblogclient-sample.jar application.properties
```
