#!/bin/bash
source=$1
dest=$2

file=$(basename $source)
basename=${file%.*}
ext=${file##*.}

if [[ ! -e "$dest/$basename.$ext" ]]; then
      mv "$source" "$dest"
else
      num=1
      while [[ -e "$dest/$basename$num.$ext" ]]; do
            (( num++ ))
      done
      mv "$source" "$dest/$basename$num.$ext" 
fi 