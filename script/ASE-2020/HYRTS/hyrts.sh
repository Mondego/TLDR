#!/bin/bash


#########################################################
# This script does the HyRTS baseline. This script #
# iterates through each project directory and runs the helper #
# . This script will write logs #
# in the LOG folder. In order to make this script place #
# this script to the home directory of this particular  #
# baseline. For example - 								#
#    RETEST-ALL/										#
#        -------- project1/								#
#		 -------- SAMPLE_COMMIT/						#
#		 -------- LOG/									#
#		 -------- hyrts.sh 
#########################################################


## Clean up
rm -rf LOG/

for num in {1..3};
do
	./helper.sh "project"$num &
done

