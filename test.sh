#!/bin/sh

CLASS_DIR=/Users/demigorgan/Desktop/Ekstazi_dataset/commons-jxpath-trunk/

output=$(mvn -q exec:java -Dexec.mainClass=uci.ics.mondego.tldr.App)
cd $CLASS_DIR

if [[ ! -z "${output// }" ]]; then
	mvn test -Dtest=$output
fi