#!/bin/sh

repo_dir=/Users/demigorgan/Desktop/commons-math

output=$(mvn -q compile exec:java -Dexec.args=$line)

cd $repo_dir

if [[ ! -z "${output// }" ]]; then
	mvn test -Dtest=$output
fi