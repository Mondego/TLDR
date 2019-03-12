#!/bin/bash

dataset_dir=$1

cd $dataset_dir

for l in $(ls)
do
	echo $l
	cd $l
	mvn clean install -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true
	mvn clean install surefire-report:report  
	mvn test -Dmaven.test.failure.ignore=true
	mvn surefire-report:report-only

	cd $dataset_dir
done