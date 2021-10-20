#!/bin/bash

cd "$(dirname "$0")/.." || exit

protoc --java_out=common/src/main/java common/src/main/java/com/oceanbase/clogproxy/common/packet/protocol/logproxy.proto
