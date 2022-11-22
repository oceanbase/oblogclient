#!/bin/bash
######################################################################################
# Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
# oblogclient is licensed under Mulan PSL v2.
# You can use this software according to the terms and conditions of the Mulan PSL v2.
# You may obtain a copy of Mulan PSL v2 at:
#          http://license.coscl.org.cn/MulanPSL2
# THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
# EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
# MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
# See the Mulan PSL v2 for more details. */
######################################################################################

cd "$(dirname "$0")/.." || exit

protoc --java_out=common/src/main/java proto/logproxy.proto
protoc --java_out=common/src/main/java proto/v1.proto
