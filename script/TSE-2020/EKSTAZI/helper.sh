#!/bin/bash

#########################################################
# This script does the Retest All baseline. This script #
# iterates through each project directory and runs retest all for #
# each commit periodically. This script will write logs #
# in the LOG folder. In order to make this script place #
# this script to the home directory of this particular  #
# baseline. For example - 								#
#    EKSTAZI/										#
#        -------- project1/								#
#		 -------- SAMPLE_COMMIT/						#
#		 -------- LOG/									#
#		 -------- retest_all.sh   
#   	 -------- MavenPOMProcessor/
# This script takes project directory as command line i #
#########################################################

base_directory=$PWD
dataset_dir=$PWD"/"$1
commit_sample_directory=$PWD"/SAMPLE_COMMIT"
maven_pom_processor="$PWD"/"MavenPOMProcessor"
log_directory=$PWD"/LOG"

# Skip all optional plugins, extensions
MAVEN_SKIPS="-Drat.skip=true -Dmaven.javadoc.skip=true \
             -Djacoco.skip=true -Dcheckstyle.skip=true \
             -Dfindbugs.skip=true -Dcobertura.skip=true \
             -Dpmd.skip=true -Dcpd.skip=true \
             -Dmaven.test.failure.ignore=true"


for project in $(ls $dataset_dir); do

	echo "*****************  "$project"  *****************"
	project_directory=$dataset_dir'/'$project

	project_log_directory=$log_directory'/'$project
	mkdir -p $project_log_directory
	
	retest_all_summary=$project_log_directory"/SUMMARY.csv"
	sample_commit=$commit_sample_directory'/'$project'.txt'

	serial=0
	while read -r line; do		
		cd $project_directory

		# needed for errorless checkout to another commit
		rm -f .git/index.lock 
	    git reset --hard $line  

	    # This line changes surefire version to make the project compatible to TLDR.
	    cd $maven_pom_processor
	    mvn -q clean compile $MAVEN_SKIPS
	    mvn -q exec:java -Dexec.args="$project_directory maven-surefire-plugin 2.19.1"
	    cd $project_directory
	    
	    if mvn -q clean compile $MAVEN_SKIPS; then			    	
	    	# If we have 20 pairs commits that builds successfully then stop. 
	    	if [ "$serial" -eq 30 ]; then
      			break
  			fi
  			
  			mvn clean
	    	((serial=serial+1))
		    			
			mvn org.ekstazi:ekstazi-maven-plugin:5.3.0:ekstazi $MAVEN_SKIPS -Dcommit.hash=$line -Dcommit.serial=$serial -Dlog.directory=$project_log_directory
			mvn surefire-report:report-only $MAVEN_SKIPS

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