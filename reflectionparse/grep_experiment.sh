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
		cd $buffer
		for claz in $(find . -type f -name "*.class");
		do
			bytecote=$(javap -c $claz)
			if echo $bytecode | grep --quiet "java.lang.Object.getClass"; then
  				echo $jar' , true' >> $summary
			  	break
			elif echo $bytecode | grep --quiet "java.lang.Class.getMethods"; then
			  echo $jar' , true' >> $summary
			  break
			elif echo $bytecode | grep --quiet "java.lang.Class.getFields"; then
			  echo $jar' , true' >> $summary
			  break
			elif echo $bytecode | grep --quiet "java.lang.Class.getMethod"; then
			  echo $jar' , true' >> $summary
			  break
			elif echo $bytecode | grep --quiet "java.lang.Class.getField"; then
			  echo $jar' , true' >> $summary
			  break
			elif echo $bytecode | grep --quiet "java.lang.Class.getConstructors"; then
			  echo $jar' , true' >> $summary
			  break
			elif echo $bytecode | grep --quiet "java.lang.Class.getConstructor"; then
			  echo $jar' , true' >> $summary
			  break
			elif echo $bytecode | grep --quiet "java.lang.reflect"; then
			  echo $jar' , true' >> $summary
			  break
			fi
		done	
		
		cd $jars'/'$dir
	done
	cd $jars
done

