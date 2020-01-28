#!/bin/bash

#experiment specific data... change at your discretion
repo_base="/Users/demigorgan/TLDR_EXP"
repo_sha_base="/Users/demigorgan/TLDR_EXP/Sampled_Commit"
repo_dir=("$repo_base/projects/commons-io" "$repo_base/projects/cucumber" "$repo_base/projects/graphhopper" "$repo_base/projects/storm" "$repo_base/projects/netty" "$repo_base/projects/jedis")
proj_dir="/Users/demigorgan/Documents/workspace/tldr"
commit_dir=("$repo_sha_base/io.txt" "$repo_sha_base/cucumber.txt" "$repo_sha_base/graphhopper.txt" "$repo_sha_base/storm.txt" "$repo_sha_base/netty.txt" "$repo_sha_base/jedis.txt")
project_name=("io" "cucumber" "graphhopper" "storm" "netty" "jedis")

# Log folder creation
foldername=$(date +%Y-%m-%d-%H-%M)
ekstazi_log_dir="$proj_dir"/"ExperimentData"/"$foldername"/"Ekstazi"
tldr_log_dir="$proj_dir"/"ExperimentData"/"$foldername"/"TLDR"
starts_log_dir="$proj_dir"/"ExperimentData"/"$foldername"/"STARTS"

mkdir -p "$ekstazi_log_dir"
mkdir -p "$tldr_log_dir"
mkdir -p "$starts_log_dir"

for index in {0..0};
do
	# clear previous results... starts over
	cd ${repo_dir[$index]}
	mvn org.ekstazi:ekstazi-maven-plugin:5.3.0:clean -Drat.ignoreErrors=true
	mvn edu.illinois:starts-maven-plugin:1.4-SNAPSHOT:clean -Drat.ignoreErrors=true
	cd $repo_base

	allSha=${commit_dir[$index]}
	while read -r line; do
		echo ${project_name[$index]}' ---- '$line

		#log files
		t_log="$tldr_log_dir"/"$line".txt
		e_log="$ekstazi_log_dir"/"$line".txt
		s_log="$starts_log_dir"/"$line".txt
	    
	    cd ${repo_dir[$index]}
	    rm -f .git/index.lock # needed for errorless checkout to another commit
	    git reset --hard $line  

	    count=`ls -1 pom.xml 2>/dev/null | wc -l`  #check if the repository includes pom.xml
	    
	    if [ $count != 0 ]; then
		    cd $proj_dir

		    mvn -q exec:java@second-cli -Dexec.args="${repo_dir[$index]} jar" # include jar plugin for test suite
		    cd ${repo_dir[$index]}
		    if mvn -q clean compile -Drat.ignoreErrors=true; then
		    	
		    	# TLDR experiment
		    	mvn -q install -Drat.ignoreErrors=true
		    	cp -R test "$proj_dir"
		    	cd $proj_dir
		    	mvn -q exec:java@second-cli -Dexec.args="${repo_dir[$index]} import"  #import test jar in tldr		    	
		    	mvn compile exec:java -Dexec.args="$t_log" # run tldr
		    	mvn -q exec:java@third-cli -Dexec.args="$t_log"  # gather meta information
		    	echo 'SIZE OF THE DATABASE : '>> $t_log
		    	du -sh ~/dump.rdb >>  $t_log

				cd ${repo_dir[$index]}

				#Ekstazi experiment
				echo "TIME : " >> $e_log
		    	{ time report_ekstazi=$(mvn org.ekstazi:ekstazi-maven-plugin:5.3.0:ekstazi -Drat.ignoreErrors=true) ; } 2> $e_log
		    	echo '=========================================================' >> $s_log
		    	echo '=========================================================' >> $s_log		    	
		    	echo $report_ekstazi >> $e_log
		    	echo '====================================================' >> $e_log
		    	echo '====================================================' >> $e_log
		    	echo "SIZE : " >> $e_log 
		    	du -sh .ekstazi/ >> $e_log

				
		    	# STARTS experiment
		    	mvn clean compile -Drat.ignoreErrors=true	
		    	echo "TIME : " >> $s_log
		    	{ time report_starts=$(mvn edu.illinois:starts-maven-plugin:1.4-SNAPSHOT:starts -Drat.ignoreErrors=true) ; } 2> $s_log
		    	echo '=========================================================' >> $s_log
		    	echo '=========================================================' >> $s_log		    	
		    	echo $report_starts >> $s_log
		    	echo '=========================================================' >> $s_log
		    	echo '=========================================================' >> $s_log		    	
		    	echo "SIZE : " >> $s_log 
		    	du -sh .starts/ >> $s_log
				
		    else
		    	echo 'BUILD FAILED'
		    fi
		else
			echo "NO POM PRESENT"
		fi

	done < "$allSha"
done

#mvn edu.illinois:starts-maven-plugin:1.4-SNAPSHOT:starts -Drat.ignoreErrors=true
#ek=$( time ( report=$( mvn org.ekstazi:ekstazi-maven-plugin:5.3.0:ekstazi -Drat.ignoreErrors=true ) ) 2>&1 1>/dev/null )


