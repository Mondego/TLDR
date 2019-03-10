#!/bin/bash

python Ramdom_commit_sample.py

repo_base="/Users/demigorgan/TLDR_EXP"
repo_sha_base="/Users/demigorgan/TLDR_EXP/Sampled_Commit"
repo_dir=("$repo_base/commons-io" "$repo_base/commons-validator" "$repo_base/commons-jxpath" "$repo_base/commons-collections" "$repo_base/commons-net")
proj_dir="/Users/demigorgan/Documents/workspace/tldr"
commit_dir=("$repo_sha_base/io.txt" "$repo_sha_base/validator.txt" "$repo_sha_base/jxpath.txt" "$repo_sha_base/collection.txt" "$repo_sha_base/net.txt")
project_name=("io" "validator" "jxpath" "collections" "net")


foldername=$(date +%Y-%m-%d-%H-%M)
mkdir -p "$proj_dir"/"$foldername"

for index in {0..4};
do
	allSha=${commit_dir[$index]}
	i=0
	while read -r line; do
		time_log="$repo_base/${project_name[$index]}".txt
		echo $line >> $time_log
		let "i++"
	    cd ${repo_dir[$index]}
	    pwd
	    rm -f .git/index.lock
	    git reset --hard $line --quiet
	     
	    if mvn -q compile ; then
	    	echo 'BUILD SUCCESSFUL FOR COMMIT : '$line
	    	if mvn -q test-compile ; then
		    	cd $proj_dir
		    	output=$(mvn -q compile exec:java -Dexec.args="$i ${repo_dir[$index]} $line")		    	
		    	#mvn -q compile exec:java -Dexec.args="$i ${repo_dir[$index]} $line"
		    	
		    	if [[ ! -z "${output// }" ]]; then
		    		cd ${repo_dir[$index]}

		    		echo TESTING TIME :  >> $time_log
		    		var="$( time ( mvn test -Dtest=$output -fae) 2>&1 1>/dev/null )"
		    		if mvn test -Dtest=$output -fae; then
		    			echo $var >> $time_log
		    		else
		    			echo BUILD FAILED >> $time_log
		    		fi
					
					cd $proj_dir
				fi		    	
		    	mv *_.txt "$proj_dir"/"$foldername"

		    else
		    	echo 'TEST BUILD FAILED FOR COMMIT : '$line
		    fi
	    
	    else 
	    	echo "BUILD FAILED FOR COMMIT : "$line
		fi
	done < "$allSha"
done