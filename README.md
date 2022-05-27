OceanBase Log Client
--------------------

OceanBase Log Client is a library for obtaining log of [OceanBase](https://github.com/oceanbase/oceanbase).

Getting Started
---------------

### Work with LogProxy

#### Use LogProxyClient

You can use `logproxy-client` with [oblogproxy](https://github.com/oceanbase/oblogproxy) to get the commit log of OceanBase cluster, see the [tutorial](docs/quickstart/logproxy-client-tutorial.md) for more details.

#### Version Compatibility

`oblogproxy` compresses the record data from `1.0.1`, and `logproxy-client` fixed the decompression process with [#33](https://github.com/oceanbase/oblogclient/pull/33) from `1.0.4`, so there is a version compatibility as follows:

|   `oblogproxy`   | `logproxy-client` |
|:----------------:|:-----------------:|
|     `1.0.0`      | `1.0.0` - `1.0.3` |
| `1.0.1` or later | `1.0.4` or later  |

### Connect to OceanBase Directly

Coming soon.

Communication
---------------
* [Official Q&A Website (Chinese)](https://open.oceanbase.com/answer) (Q&A, ideas, general discussion)
* [GitHub Issues](https://github.com/oceanbase/oblogclient/issues) (Bug reports, feature requests)
* DingTalk Group (Q&A, general discussion): 33254054

License
-------
Mulan Permissive Software License, Version 2 (Mulan PSL v2). See the [LICENSE](LICENCE) file for details.
