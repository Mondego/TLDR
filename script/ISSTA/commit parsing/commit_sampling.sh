#!/bin/bash


for i in $(ls);
do
   if [[ $i == *".txt" ]]; then	
   	  python ~/Documents/workspace/maven-tldr/script/Random_commit_pair_sample.py $PWD'/'$i $i; 
   fi

done