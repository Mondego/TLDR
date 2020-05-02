#!/bin/bash

#########################################################
# This script does the Retest All baseline. This script #
# iterates through each project directory and runs retest all for #
# each commit periodically. This script will write logs #
# in the LOG folder. In order to make this script place #
# this script to the home directory of this particular  #
# baseline. For example - 								#
#    RETEST-ALL/										#
#        -------- project1/								#
#		 -------- SAMPLE_COMMIT/						#
#		 -------- LOG/									#
#		 -------- retest_all.sh      
# This script takes project directory as command line i #
#########################################################

base_directory=$PWD
dataset_dir=$PWD"/"$1
commit_sample_directory=$PWD"/SAMPLE_COMMIT"
log_directory=$PWD"/LOG"

for project in $(ls $dataset_dir); do

	echo "*****************  "$project"  *****************"
	project_log_directory=$log_directory'/'$project
	mkdir -p $project_log_directory
	
	retest_all_summary=$project_log_directory"/SUMMARY.csv"
	sample_commit=$commit_sample_directory'/'$project'.txt'

	serial=0
	while read -r line; do		
		cd $dataset_dir'/'$project

		# needed for errorless checkout to another commit
		rm -f .git/index.lock 
	    git reset --hard $line  
	    
	    if mvn -q clean compile ; then			    	
	    	# If we have 20 pairs commits that builds successfully then stop. 
	    	if [ "$serial" -eq 10 ]; then
      			break
  			fi
  			
	    	((serial=serial+1))

	    	r=$((time mvn test -Drat.skip=true -Dmaven.test.failure.ignore=true -Dcheckstyle.skip -DforkCount=8 -DreuseForks=true -fae)2>&1)
			mvn surefire-report:report-only -Drat.skip=true -Dmaven.test.failure.ignore=true -Dcheckstyle.skip
			t=$(tail -n3 <<< "$r" | grep real)
			t=$(echo $t | awk '{print $2}')
			result=$serial','$line','$t
			echo $result >> $retest_all_summary
		    			
			num=1
	     	for surefire_report in $( find . -name surefire-report.html );
	     	do
	     		report=$project_log_directory'/'$serial'_'$line'_'$num'.html'
				cat $surefire_report >> $report
				(( num++ ))	
	     	done
		fi
	done < "$sample_commit"
	
	cd $base_directory
done
