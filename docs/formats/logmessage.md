# LogMessage

[LogMessage.java](../../oblogclient-common/src/main/java/com/oceanbase/oms/logmessage/LogMessage.java) defines `LogMessage` as the structure of the log records. During the running of the program, the client will convert the received log data into LogMessage objects, and users can use them to customize their own processing logic.

## Struct

When we fetch log data from OceanBase, the data will firstly be serialized using [oblogmsg](https://github.com/oceanbase/oblogmsg), and eventually be converted into LogMessage struct in the client. For specific field information, please refer to oblogmsg.

There are the common fields of LogMessage:

<div class="highlight">
    <table class="colwidths-auto docutils">
        <thead>
            <tr>
                <th class="text-left" style="width: 20%">Field</th>
                <th class="text-left" style="width: 20%">Getter</th>
                <th class="text-left" style="width: 20%">Type</th>
                <th class="text-left" style="width: 40%">Description</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>byteBuf</td>
                <td>getRawData</td>
                <td>byte[]</td>
                <td>The original log data in byte array format.</td>
            </tr>
            <tr>
                <td>srcType</td>
                <td>getDbType</td>
                <td>DbTypeEnum</td>
                <td>Type of datasource, OceanBase versions before 1.0 correspond to <code>OB_05</code>, versions 1.0 and later correspond to <code>OB_MYSQL</code> and <code>OB_ORACLE</code>.</td>
            </tr>
            <tr>
                <td>op</td>
                <td>getOpt</td>
                <td>DataMessage.Record.Type</td>
                <td>The type of log data, OceanBase mainly involves <code>BEGIN</code>, <code>COMMIT</code>, <code>INSERT</code>, <code>UPDATE</code>, <code>DELETE</code>, <code>DDL</code>, <code>HEARTBEAT</code>.</td>
            </tr>
            <tr>
                <td>timestamp</td>
                <td>getTimestamp</td>
                <td>String</td>
                <td>The timestamp of data change execution time.</td>
            </tr>
            <tr>
                <td>dbName</td>
                <td>getDbName</td>
                <td>String</td>
                <td>Database name of log data. Note that this value contains the tenant name in the format of <code>tenant_name.db_name</code>.</td>
            </tr>
            <tr>
                <td>tableName</td>
                <td>getTableName</td>
                <td>String</td>
                <td>Table name of log data.</td>
            </tr>
        </tbody>
    </table>
</div>

The field list of DML and DDL can be obtained through the `getFieldList` method. The following are commonly used fields in the Field struct:

<div class="highlight">
    <table class="colwidths-auto docutils">
        <thead>
            <tr>
                <th class="text-left" style="width: 10%">Field</th>
                <th class="text-left" style="width: 20%">Getter</th>
                <th class="text-left" style="width: 10%">Type</th>
                <th class="text-left" style="width: 60%">Description</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>primaryKey</td>
                <td>isPrimary</td>
                <td>boolean</td>
                <td>Flag of whether this field is a primary key of not null unique key.</td>
            </tr>
            <tr>
                <td>name</td>
                <td>getFieldname</td>
                <td>String</td>
                <td>Field name.</td>
            </tr>
            <tr>
                <td>type</td>
                <td>getType</td>
                <td>DataMessage.Record.Field.Type</td>
                <td>Field type.</td>
            </tr>
            <tr>
                <td>encoding</td>
                <td>getEncoding</td>
                <td>String</td>
                <td>Field encoding.</td>
            </tr>
            <tr>
                <td>value</td>
                <td>getValue</td>
                <td>ByteString</td>
                <td>Field value in ByteString type.</td>
            </tr>
            <tr>
                <td>prev</td>
                <td>isPrev</td>
                <td>boolean</td>
                <td>Flag of whether it is a old value. It is true if this field is the value before the change, and false if it is the value after the change.</td>
            </tr>
        </tbody>
    </table>
</div>

## Usage

Please refer to [LogProxyClientTest.java](../../oblogclient-logproxy/src/test/java/com/oceanbase/clogproxy/client/LogProxyClientTest.java).

### Safe Checkpoint

LogMessage provides `safeTimestamp` to indicate the safe checkpoint, it is a timestamp in seconds, which means all the log messages committed earlier than this timestamp have been received by the client.

```java
long checkpoint = Long.parseLong(message.getSafeTimestamp());
```

For LogMessage of OceanBase, there are two kinds of timestamp:
- HEARTBEAT type: the value of the `timestamp` field is the timestamp corresponding to the safe checkpoint.
- Other types: the value of the `timestamp` field is the execution time of the data change, and the `fileNameOffset` field corresponds to the latest HEARTBEAT timestamp. Since `libobcdc` does not guarantee that the fetched data changes are in timestamp order, so for DDL and DML types of LogMessage, `fileNameOffset` should be used as safe checkpoint instead of `timestamp`.

So you can also get the safe checkpoint of OceanBase LogMessage by the following code:

```java
long checkpoint;
if (DataMessage.Record.Type.HEARTBEAT.equals(message.getOpt())) {
    checkpoint = Long.parseLong(message.getTimestamp());
} else {
    checkpoint = message.getFileNameOffset();
}
```
