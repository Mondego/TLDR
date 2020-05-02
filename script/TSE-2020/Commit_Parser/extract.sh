#!/bin/bash

##############################################################################################
# Running this script will extract the commits for the experiment. It requires the 'projects/'
# folder to be in the same directory
#############################################################################################

current_directory=$PWD
project_directory=$current_directory"/projects"
SAMPLED_COMMIT=$current_directory"/SAMPLED_COMMIT"

rm -rf $SAMPLED_COMMIT
mkdir -p $SAMPLED_COMMIT

for i in $(ls $project_directory); 
do
	name=${i%.*}
	project=$project_directory"/"$name
	log_file=$SAMPLED_COMMIT"/"$name".txt"
	touch $log_file
	cd $project
	git log > $log_file
	cd $current_directory
	python extract_helper.py $log_file;
done