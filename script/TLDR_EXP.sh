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
Ekstazi_log="$repo_base/Ekstazi_log"/"$foldername"
mkdir -p "$Ekstazi_log"

#for each project
for index in {0..4};
do
	allSha=${commit_dir[$index]}
	i=0
	while read -r line; do
		#for tldr log
		time_log="$repo_base/${project_name[$index]}".txt
		#for ekstazi log
		time_log_ekstazi="$repo_base/${project_name[$index]}"_ekstazi.txt
		

		echo "" >> $time_log
		echo "" >> $time_log_ekstazi
		echo $line >> $time_log
		echo $line >> $time_log_ekstazi
		let "i++"
		mkdir -p "$Ekstazi_log"/"$i"/"$line"/"Ekstazi"
		mkdir -p "$Ekstazi_log"/"$i"/"$line"/"TLDR"
	    cd ${repo_dir[$index]}
	    pwd
	    rm -f .git/index.lock # needed for errorless checkout to another commit
	    git reset --hard $line --quiet

	    count=`ls -1 pom.xml 2>/dev/null | wc -l`  #check if the repository includes pom.xml
	    if [ $count != 0 ]; then
		    
		    # process the pom to sure-fire 2.7.1
		    cd $proj_dir
		    mvn -q exec:java@second-cli -Dexec.args="${repo_dir[$index]} surefire"
		    cd ${repo_dir[$index]}

		    if mvn -q clean compile ; then
		    	echo 'BUILD SUCCESSFUL FOR COMMIT : '$line
		    	if mvn -q clean test-compile ; then
			    	cd $proj_dir

			    	# getting only the output test cases.... time can be found in TLDR log
			    	output=$(mvn -q compile exec:java -Dexec.args="$i ${repo_dir[$index]} $line")		    	
			    	

			    	if [[ ! -z "${output// }" ]]; then
			    		cd ${repo_dir[$index]}
			    		
			    		var="$( time ( mvn -Dtest=$output test -Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true) 2>&1 1>/dev/null )"
			    		mvn -q surefire-report:report
			    		report=$( find target/site/* )

						if [[ ! -z "${report// }" ]] ;  then
							echo "TLDR REPORT FOUND..... MOVING TO LOG DIRECTORY"
							mv "$report" "$Ekstazi_log"/"$i"/"$line"/"TLDR"
						else
							echo 'NO REPORT FOUND FOR TLDR'
						fi

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

			    	#ekstazi experiment
			    	# include ekstazi plugin in pom
			    	mvn -q exec:java@second-cli -Dexec.args="${repo_dir[$index]} ekstazi"
			    	
			    	#compile, run, and report ekstazi
			    	cd ${repo_dir[$index]}
			    	mvn -q clean compile
			    	mvn -q clean test-compile
			    	var_ekstazi="$( time ( mvn ekstazi:ekstazi  -Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true ) 2>&1 1>/dev/null )"
			    	mvn -q surefire-report:report
			    	if [[ ! -z "${var_ekstazi// }" ]] ;  then
			    			echo "EKSTAZI EXECUTED ......"
							echo 'TESTING TIME : '$var_ekstazi >> $time_log_ekstazi
							#move surefire report to a log folder
							surefire_report=$( find target/site/* )

							if [[ ! -z "${surefire_report// }" ]] ;  then
								echo "EKSTAZI REPORT FOUND..... MOVING TO LOG DIRECTORY"
								mv "$surefire_report" "$Ekstazi_log"/"$i"/"$line"/"Ekstazi"
							else
								echo 'NO REPORT FOUND FOR EKSTAZI'
							fi

					else
							echo 'TEST CONTAINS ERROR' >> $time_log_ekstazi
					fi


			    else
			    	echo 'TEST BUILD FAILED FOR COMMIT : '$line >> $time_log
			    fi
		    
		    else 
		    	echo "BUILD FAILED FOR COMMIT : "$line >> $time_log
			fi
		fi
	done < "$allSha"
done
