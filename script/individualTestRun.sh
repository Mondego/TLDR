#!/bin/bash

# this script runs each test case individually. 
# steps: 
# 1. Run the whole test suit of a project
# 2. generate surefire-report. 
# 3. Change the directrory to the current surefire report to the Ekstazi set in SureFireReportParserUtilities.java file in TLDR. 
# 4. Run IntraTestDependencyExperiment which will generate a log.txt file inside TLDR repo
# 5. place this file inside the repo of the experiment project
# 6. place this script i.e. individualTest.sh inside the repo
# 7. run the bash file
# 8. the result will be in output.txt

while read -r line; do
    cd lang
	var=$( mvn test -Dtest=$line)
	if [[ $var == *"ERROR"* ]]; then
        echo $line"  FAILS" >> /Users/demigorgan/Desktop/common-lang-2/output.txt
	  echo $line"  FAILS"
	else
	   echo $line"  SUCCESS" >> /Users/demigorgan/Desktop/common-lang-2/output.txt
	   echo $line"  SUCCESS"
	fi
    cd ..

done < log.txt

