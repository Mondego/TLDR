#!/bin/bash

for i in $( ls REDO );
do
	mkdir -p ~/TLDR_EXP/TIMES/"$i"
	file="REDO"/"$i"/
	echo $file
	grep -r user $file > ~/TLDR_EXP/TIMES/"$i"/"$i"_user.txt
	grep -r sys $file > ~/TLDR_EXP/TIMES/"$i"/"$i"_sys.txt
	grep -r real $file > ~/TLDR_EXP/TIMES/"$i"/"$i"_real.txt   
done


print="{"

for i in $( ls REDO );
do
	print=$print"\""
	print=$print"$i"
	print=$print"\""
	print=$print","
done

print=$print"}"

echo $print
