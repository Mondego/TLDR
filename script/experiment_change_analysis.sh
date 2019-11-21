#!/bin/bash

base_dir="/lv_scratch/scratch/mondego/local/Maruf"
sample_project_dir=$base_dir'/PROJECTS'
tldr_dir=$base_dir'/TLDR'

for i in $(ls $sample_project_dir);
do
	test_project=$sample_project_dir/$i
	./change_analysis.sh $tldr_dir $test_project $i &
done



