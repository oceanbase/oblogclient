# LogMessage

LogMessage is a struct to store log messages, see the [class file](../../common/src/main/java/com/oceanbase/oms/logmessage/LogMessage.java) for its definition.

## LogMessage Struct

A LogMessage object mainly has the following fields (getter):

- *RawData*: Byte array that contains all details of the log message.
- *DbType*: Type of source database, here we only use `OCEANBASE1`, which means OceanBase 1.0 or higher version.
- *Opt*: Operation type, here should be one of `BEGIN`, `COMMIT`, `INSERT`, `UPDATE`, `DELETE`, `DDL`, `HEARTBEAT`.
- *DbName*: Database name, here it is in format of `tenant_name.database_name`.
- *TableName*: Table name.
- *Timestamp*: Timestamp in seconds.
- *OB10UniqueId*: Transaction id (string) of log message, only recorded in `BEGIN` or DML (`INSERT`, `UPDATE`, `DELETE`).
- *FieldList*: A list of row fields.

## Field List in LogMessage

The item in *FieldList* of LogMessage is of type `DataMessage.Record.Field`, and one Field corresponding to a column of one row. A Field struct mainly contains fields as following:

- *length*: The length of `value` field.
- *primaryKey*: Flag of whether the column is the primary key.
- *name*: Column name.
- *type*: Type of the column, raw value is the const in `LogMessageTypeCode`.
- *flag*: Flag of whether the Field is generated/parsed.
- *encoding*: Encoding of the column.
- *value*: Column value.
- *prev*: Flag of whether the Field is the old value of the column.

Note that the Field struct here contains the type information, which is different from MySQL binlog. The value of a Field is of `ByteString` type, which could be used as a byte array or a string, both of which can easily cast to other types.

The content of Field list in the LogMessage is related to the operation type:

 - `BEGIN`、`COMMIT`、`HEARTBEAT`：null
 - `DDL`: One Field with ddl sql in value field.
 - `INSERT`: The column value list of the new row.
 - `UPDATE`: Both the old and new column values of the row. The list should be [field_0_old, field_0_new, field_1_old, field_1_new, ...].
 - `DELETE`: The column value list of the old row.

## Usage

You can see which projects use `logproxy-client` [here](https://github.com/oceanbase/oblogclient/network/dependents?package_id=UGFja2FnZS0yODMzMjE5Nzc1).
