#!/bin/bash

repo_base="/Users/demigorgan/TLDR_EXP"
repo_dir=("$repo_base/projects/commons-compress/") 
commit=("/Users/demigorgan/TLDR_EXP/USED_COMMITS/compress.txt")
project_name=("compress")


while read -r line; do
	log="$repo_base"/"RETEST"/"${project_name[$index]}"/"$line"
	mkdir -p "$log"
    cd ${repo_dir[$index]}
    rm -f .git/index.lock # needed for errorless checkout to another commit
    git reset --hard $line  

    mvn -q clean compile -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true
    mvn -q test-compile -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true
    time mvn -q surefire-report:report -Drat.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true
    report=$( find target/site/* )
    mv "$report" "$log"
    for reports in $( find */target/site/*.html );
    do
        file=$(basename $reports)
        basename=${file%.*}
        ext=${file##*.}
        if [[ ! -e "$log/$basename.$ext" ]]; then
            mv "$reports" "$log"
        else
            num=1
            while [[ -e "$log/$basename$num.$ext" ]]; do
                (( num++ ))
            done
            mv "$reports" "$log/$basename$num.$ext" 
        fi  
    done									
done < "$commit"