#!/bin/bash

repo_dir="/Users/demigorgan/Desktop/commons-math"
proj_dir="/Users/demigorgan/Documents/workspace/tldr"

cd $proj_dir/'script'

i=0

allsha="sha.txt"

while read -r line; do
	let "i++"
    cd $repo_dir
    git checkout $line --quiet
    mvn -q compile 
    mvn -q test-compile
    cd $proj_dir
    mvn -q compile exec:java -Dexec.args=$line

done < "$allsha"