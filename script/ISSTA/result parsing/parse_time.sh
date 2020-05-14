#!/bin/bash

# in order to run this script do this -- 
# place this script to the folder where all the five baselines are 
# place surefirereportparser project in the same directory
# results will be stored in TIME-DATA folder


root_directory=$PWD
parser_directory=$root_directory'/surefirereportparser'
baselines=("TLDR" "EKSTAZI" "STARTS" "HYRTS" "1-TLDR")
log_root_dir=$PWD'/TIME-DATA'


mkdir -p $log_root_dir

if [[ ! -e $parser_directory ]]; then
	git https://github.com/marufzaber/Surefire-report-parser.git
fi

for index in {0..2};
do
	baseline=${baselines[$index]}
	mkdir -p $log_root_dir'/'$baseline
	baseline_log_dir=$log_root_dir'/'$baseline
	cd $baseline
	for project in $(ls);
	do
		echo '******* '$project' *******'
		project_directory=$root_directory'/'$baseline'/'$project
		cd $parser_directory
		mvn -q compile exec:java@second-cli -Dexec.args="$project_directory $baseline_log_dir'/'$project'-SUMMARY.csv'"
		cd $root_directory'/'$baseline
		echo '******* DONE *******'
	done
	cd $root_directory
done
