#!/bin/bash
#
# LanguageTool, a natural language style checker
# Copyright (C) 2021 Stefan Viol (https://stevio.de)
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
# USA
#
if [ $# -ne 1 ]; then
  echo "Usage: `basename $0` Version Test"
  echo "Examples:"
  echo "  ./`basename $0` full"
  echo "      Create snapshot and runs all tests"
  echo "  ./`basename $0` none"
  echo "      Create snapshot and runs no tests"  
  echo "  ./`basename $0` fae"
  echo "      Create snapshot and runs tests with fail-at-end"  
  echo "  ./`basename $0` fn"
  echo "      Create snapshot and runs tests with fail-never"
  exit 1
fi

TEST=$1

case "$TEST" in
  "fae" | "fn")
    COMMAND="mvn clean package -$TEST"
    echo "$COMMAND"
    $COMMAND
    ;;
  "none")
    COMMAND="mvn clean package -DskipTests"
    echo "$COMMAND"
    $COMMAND  
    ;;
  "full")
    COMMAND="mvn clean package"
    echo "$COMMAND"
    $COMMAND
    ;;
  *)
    echo "Invalid test argument"
    ;;
esac
exit 0