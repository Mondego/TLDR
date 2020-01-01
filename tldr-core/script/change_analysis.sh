#!/bin/bash

tldr_dir=$1
repo_dir=$2
test_project_name=$3

cd $repo_dir

sha_file=$tldr_dir'/script/'$test_project_name'.txt'

echo $sha_file
ls

git log > $sha_file

cd $tldr_dir/'script'

python extract_sha.py $test_project_name'.txt' $test_project_name

sha=$tldr_dir'/script/'$test_project_name'_sha.txt'

while read -r line; do
	echo $line
	echo '*************************************'
	echo '*************************************'

    cd $repo_dir
    rm -f .git/index.lock # needed for errorless checkout to another commit
    git reset --hard $line 
	count=`ls -1 pom.xml 2>/dev/null | wc -l`
	compile_info=$test_project_name'-compile-info.csv'
	if [ $count != 0 ]; then
	    if mvn -q clean compile ; then
	    	echo 'BUILD SUCCESSFUL FOR COMMIT : '$line
	    	cd $tldr_dir
	    	echo $line', compiled' >> $compile_info
	    	mvn -q compile
	    	mvn exec:java@fourth-cli -Dexec.args="$repo_dir $test_project_name $line"
	    fi
	else 
		cd $tldr_dir
		echo $line', not compiled' >> $compile_info
	fi
done < "$sha"

