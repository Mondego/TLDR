#!/bin/bash


while read -r line; do

var=$( mvn test -Dtest=$line)
if [[ $var == *"ERROR"* ]]; then
  echo $line"  FAILS"
fi

done < log.txt

