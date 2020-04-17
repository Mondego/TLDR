#!/bin/bash


#########################################################
# This script does the Retest All baseline. This script #
# iterates through each project directory and runs the helper #
# . This script will write logs #
# in the LOG folder. In order to make this script place #
# this script to the home directory of this particular  #
# baseline. For example - 								#
#    RETEST-ALL/										#
#        -------- project1/								#
#		 -------- SAMPLE_COMMIT/						#
#		 -------- LOG/									#
#		 -------- retest_all.sh 
#########################################################


## Clean up
rm -rf LOG/ 

parser_directory=$root_directory'/surefirereportparser'
maven_pom_processor="$PWD"/"MavenPOMProcessor"

if [[ ! -e $maven_pom_processor ]]; then
	git clone https://github.com/marufzaber/MavenPOMProcessor.git
fi

for num in {1..1};
do
	./helper.sh "project"$num 
done

