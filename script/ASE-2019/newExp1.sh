#!/bin/bash

repo_base="/Users/demigorgan/TLDR_EXP"
repo_sha_base="/Users/demigorgan/TLDR_EXP/Sampled_Commit"
repo_dir=("$repo_base/projects/commons-math" "$repo_base/projects/commons-codec" "$repo_base/projects/bval" "$repo_base/projects/commons-pool" "$repo_base/projects/closure-compiler") 
proj_dir="/Users/demigorgan/Documents/workspace/tldr"
commit_dir=("$repo_sha_base/math.txt" "$repo_sha_base/codec.txt" "$repo_sha_base/bval.txt" "$repo_sha_base/pool.txt" "$repo_sha_base/compiler.txt")
project_name=("math" "codec" "bval" "pool" "compiler")


for index in {0..0};
do
	allSha=${commit_dir[$index]}
	i=0
	rm -rf ~/dump.rdb 
	foldername=$(date +%Y-%m-%d-%H-%M)
	ekstazi_log_dir="$repo_base"/"ExperimentData"/"${project_name[$index]}"/"$foldername"/"Ekstazi"
	tldr_log_dir="$repo_base"/"ExperimentData"/"${project_name[$index]}"/"$foldername"/"TLDR"
	starts_log_dir="$repo_base"/"ExperimentData"/"${project_name[$index]}"/"$foldername"/"STARTS"
	mkdir -p "$ekstazi_log_dir"
	mkdir -p "$tldr_log_dir"
	mkdir -p "$starts_log_dir"

	cd ${repo_dir[$index]}
	mvn org.ekstazi:ekstazi-maven-plugin:5.3.0:clean -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true
	mvn edu.illinois:starts-maven-plugin:1.4-SNAPSHOT:clean -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true

	while read -r line; do
		echo ${project_name[$index]}' ---- '$line

		let "i++"

		t_log="$tldr_log_dir"/"$i"_"$line".txt
		e_log="$ekstazi_log_dir"/"$i"_"$line".txt
		s_log="$starts_log_dir"/"$i"_"$line".txt

	    cd ${repo_dir[$index]}
	    rm -f .git/index.lock # needed for errorless checkout to another commit
	    git reset --hard $line  

	    count=`ls -1 pom.xml 2>/dev/null | wc -l`  #check if the repository includes pom.xml
	    if [ $count != 0 ]; then

		    cd $proj_dir
		    #mvn -q exec:java@second-cli -Dexec.args="${repo_dir[$index]} surefire"
		    cd ${repo_dir[$index]}

		    if mvn -q clean compile -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true; then
		    	if mvn -q test-compile -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true; then
			    	cd $proj_dir

			    	echo "TIME : " >> $t_log

			    	{ time output=$(mvn -q compile exec:java -Dexec.args="$i ${repo_dir[$index]} $line maven") ; } 2> $t_log	    	
			    	
			    	if [[ ! -z "${output// }" ]]; then
			    		cd ${repo_dir[$index]}
			    		for test in $output; 
			    		do

			    			if [[ ! -z "${test// }" ]]; then
			    				report_tldr=$(mvn test -Dtest=$test -DfailIfNoTests=false -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true -fae)
			    				echo '=========================================================' >> $t_log
			    				echo '=========================================================' >> $t_log		    	
			    				echo $report_tldr >> $t_log
			    				echo '====================================================' >> $t_log
			    				echo '====================================================' >> $t_log
			    									
							fi
						done
					else
						echo 'No TEST SELECTED' > $t_log
					fi	
			    	
			    	#Ekstazi experiment
					echo "TIME : " >> $e_log
			    	{ time report_ekstazi=$(mvn org.ekstazi:ekstazi-maven-plugin:5.3.0:ekstazi -DfailIfNoTests=false -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true -fae) ; } 2> $e_log
			    	
			    	echo 'EKSTAZI ****************************'
			    	#mvn -q org.ekstazi:ekstazi-maven-plugin:5.3.0:ekstazi -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true
			    	echo '=========================================================' >> $e_log
			    	echo '=========================================================' >> $e_log		    	
			    	echo $report_ekstazi >> $e_log
			    	echo '====================================================' >> $e_log
			    	echo '====================================================' >> $e_log
			    	
			    	# STARTS experiment		    	
			    	echo "TIME : " >> $s_log
			    	{ time report_starts=$(mvn edu.illinois:starts-maven-plugin:1.4-SNAPSHOT:starts -DfailIfNoTests=false -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true -fae) ; } 2> $s_log
			    	
			    	echo 'STARTS ****************************'
			    	#mvn -q edu.illinois:starts-maven-plugin:1.4-SNAPSHOT:starts -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true
			    	echo '=========================================================' >> $s_log
			    	echo '=========================================================' >> $s_log		    	
			    	echo $report_starts >> $s_log
			    	echo '=========================================================' >> $s_log
			    	echo '=========================================================' >> $s_log		    

			    fi
			fi
		fi

	done < "$allSha"
done

