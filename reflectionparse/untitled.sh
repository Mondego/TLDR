#!/bin/bash


parser='/scratch/mondego/local/Maruf/reflectionparse'
jars='/extra/lopes1/mondego-data/backups/pedro/50K-UCLA/50K-UCLA/builds'
buffer='/scratch/mondego/local/Maruf/TEMP'
summary='/scratch/mondego/local/Maruf/SUMMARY.csv'

cd $jars
for dir in $(ls);
do
	cd $dir
	for jar in $(ls);
	do
		echo $jar
		d=$PWD
		cd $buffer
		rm -rf *
		cd $d
		cp -r $jar $buffer		
		cd $parser
		result=$(mvn -q compile exec:java -Dexec.args="$buffer")
		echo $jar' , '$result >> $summary
		cd $jars'/'$dir
	done
	cd $jars
done

