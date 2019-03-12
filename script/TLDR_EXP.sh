#!/bin/bash

#parse and sample commit hash from commit log
python Ramdom_commit_sample.py

#experiment specific data... change at your discretion
repo_base="/Users/demigorgan/TLDR_EXP"
repo_sha_base="/Users/demigorgan/TLDR_EXP/Sampled_Commit"
repo_dir=("$repo_base/commons-io" "$repo_base/commons-validator" "$repo_base/commons-jxpath" "$repo_base/commons-collections" "$repo_base/commons-net")
proj_dir="/Users/demigorgan/Documents/workspace/tldr"
commit_dir=("$repo_sha_base/io.txt" "$repo_sha_base/validator.txt" "$repo_sha_base/jxpath.txt" "$repo_sha_base/collection.txt" "$repo_sha_base/net.txt")
project_name=("io" "validator" "jxpath" "collections" "net")

# TLDR log folder creation
foldername=$(date +%Y-%m-%d-%H-%M)
mkdir -p "$proj_dir"/"$foldername"

#for each project
for index in {0..4};
do
	allSha=${commit_dir[$index]}
	i=0
	while read -r line; do
		time_log="$repo_base/${project_name[$index]}".txt
		echo ""
		echo $line >> $time_log
		let "i++"
	    cd ${repo_dir[$index]}
	    pwd
	    rm -f .git/index.lock # needed for errorless checkout to another commit
	    git reset --hard $line --quiet

	    count=`ls -1 pom.xml 2>/dev/null | wc -l`  #check if the repository includes pom.xml
	    if [ $count != 0 ]; then
		    cd $proj_dir
		    # process the pom to sure-fire 2.12.1
		    mvn -q exec:java@second-cli -Dexec.args="${repo_dir[$index]} surefire"
		    cd ${repo_dir[$index]}

		    if mvn -q clean compile ; then
		    	echo 'BUILD SUCCESSFUL FOR COMMIT : '$line
		    	if mvn -q clean test-compile ; then
			    	cd $proj_dir
			    	output=$(mvn -q compile exec:java -Dexec.args="$i ${repo_dir[$index]} $line")		    	
			    	
			    	if [[ ! -z "${output// }" ]]; then
			    		cd ${repo_dir[$index]}
			    		
			    		var="$( time ( mvn -Dtest=$output clean test -fae) 2>&1 1>/dev/null )"
			    		mvn -Dtest=$output test -fae
			    		if [[ ! -z "${var// }" ]] ;  then
							echo 'TESTING TIME : '$var >> $time_log
						else
							echo 'TEST CONTAINS ERROR' >> $time_log
						fi
					else
						echo 'NO TEST WAS RUN' >> $time_log
					fi	
					cd $proj_dir	    	
			    	mv *_.txt "$proj_dir"/"$foldername"

			    else
			    	echo 'TEST BUILD FAILED FOR COMMIT : '$line >> $time_log
			    fi
		    
		    else 
		    	echo "BUILD FAILED FOR COMMIT : "$line >> $time_log
			fi
		fi
	done < "$allSha"
done