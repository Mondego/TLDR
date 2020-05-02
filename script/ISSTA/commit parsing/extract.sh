
#!/bin/bash

for i in $(ls); 

do
	name=${i%.*}
	python ~/Documents/workspace/maven-tldr/script/extract_sha.py $PWD'/'$i $name; 
done