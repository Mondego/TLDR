#!/bin/bash

proj_dir=$1
repo_dir=$2
test_project_name=$3

cd $repo_dir

git log > $proj_dir'/script/repo_log.txt'

cd $proj_dir/'script'

python extract_sha.py repo_log.txt

sha=$proj_dir'/script/sha.txt'

while read -r line; do
	echo $line
	echo '*************************************'
	echo '*************************************'

    cd $repo_dir
    rm -f .git/index.lock # needed for errorless checkout to another commit
    git reset --hard $line 
	count=`ls -1 pom.xml 2>/dev/null | wc -l`
	echo $count
	if [ $count != 0 ]; then
	    if mvn -q clean compile ; then
	    	echo 'BUILD SUCCESSFUL FOR COMMIT : '$line
	    	cd $proj_dir
	    	echo $line', compiled' >> compilation-info.csv
	    	mvn -q compile
	    	mvn exec:java@fourth-cli -Dexec.args="$repo_dir $test_project_name $line"
	    fi
	else 
		cd $proj_dir
		echo $line', not compiled' >> compilation-info.csv
	fi
done < "$sha"

rm -rf $repo_dir

