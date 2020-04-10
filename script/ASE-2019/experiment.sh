#!/bin/bash

#parse and sample commit hash from commit log

python Ramdom_commit_sample.py

#python seq_commit_sample.py

#experiment specific data... change at your discretion
repo_base="/Users/demigorgan/TLDR_EXP"
repo_sha_base="/Users/demigorgan/TLDR_EXP/Sampled_Commit"
repo_dir=("$repo_base/projects/commons-math" "$repo_base/projects/cucumber" "$repo_base/projects/graphhopper" "$repo_base/projects/storm" "$repo_base/projects/netty" "$repo_base/projects/jedis")
proj_dir="/Users/demigorgan/Documents/workspace/tldr"
commit_dir=("$repo_sha_base/math.txt" "$repo_sha_base/cucumber.txt" "$repo_sha_base/graphhopper.txt" "$repo_sha_base/storm.txt" "$repo_sha_base/netty.txt" "$repo_sha_base/jedis.txt")
project_name=("math" "cucumber" "graphhopper" "storm" "netty" "jedis")

# TLDR log folder creation
foldername=$(date +%Y-%m-%d-%H-%M)
mkdir -p "$proj_dir"/"$foldername"
Ekstazi_log="$repo_base"/"Ekstazi_log"/"$foldername"
mkdir -p "$Ekstazi_log"

#for each project
for index in {0..0};
do
	allSha=${commit_dir[$index]}
	i=0
	while read -r line; do
		echo ${project_name[$index]}' ---- '$line
		ekstazi_report=$Ekstazi_log/${project_name[$index]}/$i/$line/"Ekstazi"
		tldr_report=$Ekstazi_log/${project_name[$index]}/$i/$line/"TLDR"
		complete_report=$Ekstazi_log/${project_name[$index]}/$i/$line/"Complete"

		mkdir -p $ekstazi_report
		mkdir -p $tldr_report
		mkdir -p $complete_report

		#for tldr log
		time_log="$Ekstazi_log/${project_name[$index]}/${project_name[$index]}"_tldr.txt
		#for ekstazi log
		time_log_ekstazi="$Ekstazi_log/${project_name[$index]}/${project_name[$index]}"_ekstazi.txt

		echo "" >> $time_log
		echo "" >> $time_log_ekstazi
		echo $line >> $time_log
		echo $line >> $time_log_ekstazi
		let "i++"

	    cd ${repo_dir[$index]}
	    rm -f .git/index.lock # needed for errorless checkout to another commit
	    git reset --hard $line  

	    count=`ls -1 pom.xml 2>/dev/null | wc -l`  #check if the repository includes pom.xml
	    if [ $count != 0 ]; then
		    # process the pom to sure-fire 2.7.1
		    cd $proj_dir
		    echo 'UPDATING SUREFIRE VERSION'
		    mvn -q exec:java@second-cli -Dexec.args="${repo_dir[$index]} surefire"
		    cd ${repo_dir[$index]}

		    if mvn  clean compile ; then
		    	echo 'BUILD SUCCESSFUL FOR COMMIT : '$line
		    	
		    	#mvn -q test -fae
		    	#mvn -q surefire-report:report -fae
    			#report=$( find target/site/* )

				#if [[ ! -z "${report// }" ]] ;  then
				#	echo "COMPLETE REPORT FOUND..... MOVING TO LOG DIRECTORY"
				#	mv "$report" "$complete_report"
				
				#else
				#	echo 'NO REPORT FOUND FOR COMPLETE'
				#fi
		    	if mvn  test-compile ; then
			    	cd $proj_dir
			    	# getting only the output test cases.... time can be found in TLDR log
			    	echo 'RUNNING TLDR'
			    	output=$(mvn -q compile exec:java -Dexec.args="$i ${repo_dir[$index]} $line maven")		    	
			    	echo $output

			    	if [[ ! -z "${output// }" ]]; then
			    		cd ${repo_dir[$index]}
			    		for test in $output; 
			    		do
			    			if [[ ! -z "${test// }" ]]; then
			    				mvn clean
			    				echo 'RUNNING TESTS FOR TLDR'
			    				#var="$( time ( mvn -Dtest=$test test -fae) 2>&1 1>/dev/null )"
					    		mvn surefire-report:report -Dtest=$test -fae
			    				report=$( find target/site/* )

								if [[ ! -z "${report// }" ]] ;  then
									echo "TLDR REPORT FOUND..... MOVING TO LOG DIRECTORY"									
									file=$(basename $report)
									basename=${file%.*}
									ext=${file##*.}

									if [[ ! -e "$tldr_report/$basename.$ext" ]]; then
									      mv "$report" "$tldr_report"
									else
									      num=1
									      while [[ -e "$tldr_report/$basename$num.$ext" ]]; do
									            (( num++ ))
									      done
									      mv "$report" "$tldr_report/$basename$num.$ext" 
									fi 									 
								else
									echo 'NO REPORT FOUND FOR TLDR'
								fi

					    		if [[ ! -z "${var// }" ]] ;  then
									echo 'TESTING TIME : '$var >> $time_log
								else
									echo 'TEST CONTAINS ERROR' >> $time_log
								fi
							fi
						done
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
			    	var_ekstazi="$( time ( mvn ekstazi:ekstazi -fae ) 2>&1 1>/dev/null )"
			    	mvn -q surefire-report:report
			    	if [[ ! -z "${var_ekstazi// }" ]] ;  then
			    			echo "EKSTAZI EXECUTED ......"
							echo 'TESTING TIME : '$var_ekstazi >> $time_log_ekstazi
							#move surefire report to a log folder
							surefire_report=$( find target/site/* )

							if [[ ! -z "${surefire_report// }" ]] ;  then
								echo "EKSTAZI REPORT FOUND..... MOVING TO LOG DIRECTORY"
								mv "$surefire_report" $ekstazi_report
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

