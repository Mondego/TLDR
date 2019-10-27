#!/bin/bash

base_dir="/scratch/mondego/local/Maruf"
sample_project_dir=$base_dir'/PROJECT'
tldr_dir=$base_dir'/TLDR'

for i in $(ls $sample_project_dir);
do
	echo $i
done



