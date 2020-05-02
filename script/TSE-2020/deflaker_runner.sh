#!/bin/bash

base_directory=$PWD
dataset_dir=$PWD"/projects"
commit_sample_directory=$PWD"/SAMPLE_COMMIT"
log_directory=$PWD"/LOG"
tldr_directory=$PWD'/TLDR/tldr-core'

for project in $(ls $dataset_dir); do

	project_log_directory=$log_directory'/'$project
	mkdir -p $project_log_directory
	
	sample_commit=$commit_sample_directory'/'$project'.txt'

	serial=1
	while read -r line; do		
		cd $dataset_dir'/'$project
		rm -f .git/index.lock 
	    git reset --hard $line  
	    
	    if mvn clean compile -Drat.skip=true -Dcheckstyle.skip=true; then		
	    	
	    	if [ "$serial" -eq 20 ]; then
      			break
  			fi
  			
  			report_file=$project_log_directory'/'$serial'_'$line'.txt'

	    	((serial=serial+1))
		    
		    cd $tldr_directory
	    	mvn -q exec:java@second-cli -Dexec.args="$dataset_dir'/'$project surefire" -X
			mvn -q exec:java@second-cli -Dexec.args="$dataset_dir'/'$project deflaker" -X
			cd $dataset_dir'/'$project
			
			mvn verify -Drat.skip=true -Dmaven.test.failure.ignore=true -Dcheckstyle.skip=true -fae > $report_file
		fi
	done < "$sample_commit"
		
	cd $base_directory
done