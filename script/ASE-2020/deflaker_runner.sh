#!/bin/bash

base_directory=$PWD
dataset_dir=$PWD"/projects"
commit_sample_directory=$PWD"/SAMPLE_COMMIT"
log_directory=$PWD"/LOG"
tldr_directory=$PWD'/TLDR'

for project in $(ls $dataset_dir); do

	project_log_directory=$log_directory'/'$project
	mkdir -p $project_log_directory
	
	sample_commit=$commit_sample_directory'/'$project'.txt'

	serial=0
	while read -r line; do		
		cd $dataset_dir'/'$project

		# needed for errorless checkout to another commit
		rm -f .git/index.lock 
	    git reset --hard $line  
	    
	    if mvn -q clean compile ; then		
	    	
	    	if [ "$serial" -eq 20 ]; then
      			break
  			fi
  			
  			report_file=$project_log_directory'/'$serial'_'$line'.txt'

  			mvn clean
	    	((serial=serial+1))
		    
	    	mvn -q exec:java@second-cli -Dexec.args="$dataset_dir'/'$project surefire"
			mvn -q exec:java@second-cli -Dexec.args="$dataset_dir'/'$project deflaker"

			cd $dataset_dir'/'$project
			report=$(mvn verify -Drat.skip=true -Dmaven.test.failure.ignore=true -Dcheckstyle.skip -fae)
			$report > $report_file

		fi
	done < "$sample_commit"
	
	cd $base_directory
done






