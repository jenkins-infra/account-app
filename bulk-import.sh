#!/bin/bash
exec mvn -e compile exec:java -Dexec.mainClass=BulkImport "-Dexec.args=$1" -Dexec.classpathScope=test
