#!/bin/bash

#experiment specific data... change at your discretion
repo_base="/Users/demigorgan/TLDR_EXP"
repo_sha_base="/Users/demigorgan/TLDR_EXP/Sampled_Commit"
repo_dir=("$repo_base/projects/commons-collections" "$repo_base/projects/NEW/commons-cli" "$repo_base/projects/NEW/commons-fileupload" "$repo_base/projects/NEW/OpenTripPlanner") 
proj_dir="/Users/demigorgan/Documents/workspace/tldr"
commit_dir=("$repo_sha_base/output1.txt" "$repo_sha_base/cli.txt" "$repo_sha_base/fileupload.txt" "$repo_sha_base/tripplaner.txt")
project_name=("MARUF" "cli" "fileupload" "tripplaner")

# Log folder creation
foldername=$(date +%Y-%m-%d-%H-%M)

for index in {0..0};
do
	# clear previous results... starts over
	rm -rf ~/dump.rdb

	
	tldr_log_dir="$repo_base"/"ExperimentData"/"${project_name[$index]}"/"$foldername"/"TLDR"
	


	mkdir -p "$tldr_log_dir"
	

	cd ${repo_dir[$index]}

	cd $repo_base

	allSha=${commit_dir[$index]}
	i=0

	while read -r line; do
		echo ${project_name[$index]}' ---- '$line
		let "i++"
		#log files
		t_log="$tldr_log_dir"/"$i"_"$line".txt
		
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
		    	{ time report_tldr=$(mvn -q com.modego.ics.uci:tldr-maven-plugin:0.0.1-SNAPSHOT:ole -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true) ; } 2> $t_log
		    	echo '=========================================================' >> $t_log
		    	echo '=========================================================' >> $t_log		    	
		    	echo $report_tldr >> $t_log
		    	echo '====================================================' >> $t_log
		    	echo '====================================================' >> $t_log

					    	
		    
		else
			echo "NO POM PRESENT"
		fi

	done < "$allSha"
done
