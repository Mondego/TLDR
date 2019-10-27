#!/bin/bash

base_dir="/scratch/mondego/local/Maruf"
sample_project_dir=$base_dir'/PROJECTS'
tldr_dir=$base_dir'/TLDR'

for i in $(ls $sample_project_dir);
do
	test_project=$sample_project_dir/$i
	echo $i 
	echo $test_project
done



