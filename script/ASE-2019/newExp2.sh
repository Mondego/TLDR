#!/bin/bash

#experiment specific data... change at your discretion
repo_base="/Users/demigorgan/TLDR_EXP"
repo_sha_base="/Users/demigorgan/TLDR_EXP/Sampled_Commit"
repo_dir=("$repo_base/projects/commons-validator" "$repo_base/projects/cucumber")
proj_dir="/Users/demigorgan/Documents/workspace/tldr"
commit_dir=("$repo_sha_base/validator.txt" "$repo_sha_base/cucumber.txt")
project_name=("validator" "cucumber" )

# Log folder creation
foldername=$(date +%Y-%m-%d-%H-%M)
ekstazi_log_dir="$repo_base"/"ExperimentData"/"$foldername"/"Ekstazi"
tldr_log_dir="$repo_base"/"ExperimentData"/"$foldername"/"TLDR"
starts_log_dir="$repo_base"/"ExperimentData"/"$foldername"/"STARTS"

mkdir -p "$ekstazi_log_dir"
mkdir -p "$tldr_log_dir"
mkdir -p "$starts_log_dir"

for index in {0..0};
do
	# clear previous results... starts over
	cd ${repo_dir[$index]}
	mvn org.ekstazi:ekstazi-maven-plugin:5.3.0:clean -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true
	mvn edu.illinois:starts-maven-plugin:1.4-SNAPSHOT:clean -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true
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
	    		cd ${repo_dir[$index]}		    		         		    		
	    	    mvn clean compile -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true
	    	    mvn test-compile -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true			

				# TLDR experiment
				echo "TIME : " >> $t_log
		    	{ time report_tldr=$(mvn com.modego.ics.uci:tldr-maven-plugin:0.0.1-SNAPSHOT:ole -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true) ; } 2> $t_log
		    	echo '=========================================================' >> $t_log
		    	echo '=========================================================' >> $t_log		    	
		    	echo $report_tldr >> $t_log
		    	echo '====================================================' >> $t_log
		    	echo '====================================================' >> $t_log

				#Ekstazi experiment
				echo "TIME : " >> $e_log
		    	{ time report_ekstazi=$(mvn org.ekstazi:ekstazi-maven-plugin:5.3.0:ekstazi -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true) ; } 2> $e_log
		    	echo '=========================================================' >> $e_log
		    	echo '=========================================================' >> $e_log		    	
		    	echo $report_ekstazi >> $e_log
		    	echo '====================================================' >> $e_log
		    	echo '====================================================' >> $e_log
		    	
		    	# STARTS experiment		    	
		    	echo "TIME : " >> $s_log
		    	{ time report_starts=$(mvn edu.illinois:starts-maven-plugin:1.4-SNAPSHOT:starts -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true) ; } 2> $s_log
		    	echo '=========================================================' >> $s_log
		    	echo '=========================================================' >> $s_log		    	
		    	echo $report_starts >> $s_log
		    	echo '=========================================================' >> $s_log
		    	echo '=========================================================' >> $s_log		    	
		    
		else
			echo "NO POM PRESENT"
		fi

	done < "$allSha"
done
