#!/bin/bash


#########################################################
# This script does the Retest All baseline. This script #
# iterates through each project directory and runs the helper #
# . This script will write logs #
# in the LOG folder. In order to make this script place #
# this script to the home directory of this particular  #
# baseline. For example - 								#
#    EKSTAZI/										#
#        -------- projects/								#
#		 -------- SAMPLE_COMMIT/						#
#		 -------- LOG/									#
#		 -------- ekstazi.sh 
#		 -------- helper.sh
#########################################################


## Clean up
rm -rf LOG/

./helper.sh "projects"

