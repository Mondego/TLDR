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
    cd $proj_dir
    #mvn -q compile exec:java -Dexec.args=$line
    SECONDS=0
	output=$(mvn -q compile exec:java -Dexec.args=$line)
	cd $repo_dir
	if [[ ! -z "${output// }" ]]; then
		mvn test -Dtest=$output -Dmaven.test.failure.ignore=true
	fi
	duration=$SECONDS
	cd $proj_dir
	echo "$line  ------ $(($duration / 60))m and $(($duration % 60))s" >> commit_vs_time.txt
done < "$allsha"