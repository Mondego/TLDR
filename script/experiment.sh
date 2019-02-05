#!/bin/bash

repo_dir="/Users/demigorgan/commons-configuration"
proj_dir="/Users/demigorgan/Documents/workspace/tldr"

cd $repo_dir

git log > $proj_dir'/script/repo_log.txt'

cd $proj_dir/'script'

python extract_sha.py repo_log.txt

i=0

allsha="sha.txt"

while read -r line; do
	let "i++"
    echo "$i - $line"
    cd $repo_dir
    git checkout $line --quiet
    mvn -q compile 
    cd $proj_dir
    mvn -q compile exec:java -Dexec.args=$line
    #mvn -q clean compile exec:java -Dexec.args=$line

done < "$allsha"