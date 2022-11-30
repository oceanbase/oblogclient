# LogMessage

[LogMessage.java](../../oblogclient-common/src/main/java/com/oceanbase/oms/logmessage/LogMessage.java) 将日志数据的结构定义为 LogMessage。在程序运行过程中，客户端会将接收到的日志数据转换成 LogMessage 对象，用户可以使用它们来定制自己的处理逻辑。

## 结构

在获取增量日志的链路中，数据先使用 [oblogmsg](https://github.com/oceanbase/oblogmsg) 进行序列化处理，之后经过传输组件最终到达客户端，再在客户端中进行反序列化，转为 LogMessage 结构。具体的字段信息可以参考 oblogmsg。

以下是 LogMessage 中常用的一些字段:

<div class="highlight">
    <table class="colwidths-auto docutils">
        <thead>
            <tr>
                <th class="text-left" style="width: 20%">参数</th>
                <th class="text-left" style="width: 20%">获取方法</th>
                <th class="text-left" style="width: 20%">返回类型</th>
                <th class="text-left" style="width: 40%">参数说明</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>byteBuf</td>
                <td>getRawData</td>
                <td>byte[]</td>
                <td>客户端接收到的日志数据原始值。</td>
            </tr>
            <tr>
                <td>srcType</td>
                <td>getDbType</td>
                <td>DbTypeEnum</td>
                <td>数据源类型，OceanBase 1.0 以前版本对应值 <code>OB_05</code>，1.0 及之后的版本对应 <code>OB_MYSQL</code> 和 <code>OB_ORACLE</code>。</td>
            </tr>
            <tr>
                <td>op</td>
                <td>getOpt</td>
                <td>DataMessage.Record.Type</td>
                <td>日志数据的类型，OceanBase 中主要涉及 <code>BEGIN</code>, <code>COMMIT</code>, <code>INSERT</code>, <code>UPDATE</code>, <code>DELETE</code>, <code>DDL</code>, <code>HEARTBEAT</code>。</td>
            </tr>
            <tr>
                <td>timestamp</td>
                <td>getTimestamp</td>
                <td>String</td>
                <td>日志数据对应的变动执行时间的时间戳。</td>
            </tr>
            <tr>
                <td>dbName</td>
                <td>getDbName</td>
                <td>String</td>
                <td>日志数据对应的库名。需要注意得是，该值包含租户名，格式为 <code>租户名.库名</code>。</td>
            </tr>
            <tr>
                <td>tableName</td>
                <td>getTableName</td>
                <td>String</td>
                <td>日志数据对应的表名。</td>
            </tr>
        </tbody>
    </table>
</div>

除此之外，可以通过 `getFieldList` 方法获取到 DML 和 DDL 的具体变动信息。以下是 Field 格式常用的字段：

<div class="highlight">
    <table class="colwidths-auto docutils">
        <thead>
            <tr>
                <th class="text-left" style="width: 10%">参数</th>
                <th class="text-left" style="width: 20%">获取方法</th>
                <th class="text-left" style="width: 10%">返回类型</th>
                <th class="text-left" style="width: 60%">参数说明</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>primaryKey</td>
                <td>isPrimary</td>
                <td>boolean</td>
                <td>是否是主键或非空唯一键。</td>
            </tr>
            <tr>
                <td>name</td>
                <td>getFieldname</td>
                <td>String</td>
                <td>字段名称。</td>
            </tr>
            <tr>
                <td>type</td>
                <td>getType</td>
                <td>DataMessage.Record.Field.Type</td>
                <td>字段类型。</td>
            </tr>
            <tr>
                <td>encoding</td>
                <td>getEncoding</td>
                <td>String</td>
                <td>字段编码。</td>
            </tr>
            <tr>
                <td>value</td>
                <td>getValue</td>
                <td>ByteString</td>
                <td>字段值，ByteString 类型。</td>
            </tr>
            <tr>
                <td>prev</td>
                <td>isPrev</td>
                <td>boolean</td>
                <td>新旧值标识，为 true 时表示该值为变更前的值，false 则为变更后的值。</td>
            </tr>
        </tbody>
    </table>
</div>

## 使用

使用示例可以参考 [LogProxyClientTest.java](../../oblogclient-logproxy/src/test/java/com/oceanbase/clogproxy/client/LogProxyClientTest.java)。

### 安全位点

LogMessage 提供了 `safeTimestamp` 来表示数据接收的安全位点，也就是说早于该秒级时间戳提交的 LogMessage 均已被客户端接收。

业务应用在进行数据消费时，一般还要维护一个数据处理的安全位点。在 LogMessage 中，该安全位点需要借助心跳的 `timestamp` 来实现。 LogMessage 在时间存储上有两套逻辑：
- 心跳类型：`timestamp` 字段值为安全位点对应的秒级时间戳。
- 其他类型：`timestamp` 字段值为数据变动的提交时间，而 `fileNameOffset` 字段对应最近一次心跳信息的 `timestamp`。由于 libobcdc 并不保证拉取到的数据变动是严格按照时间顺序的，因此对于 DDL、DML 类型的 LogMessage，应当使用 `fileNameOffset` 而非 `timestamp` 作为安全位点。

获取当前数据对应安全位点可以使用如下代码：

```java
long checkpoint;
if (DataMessage.Record.Type.HEARTBEAT.equals(message.getOpt())) {
    checkpoint = Long.parseLong(message.getTimestamp());
} else {
    checkpoint = message.getFileNameOffset();
}
```



