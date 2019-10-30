#!/usr/bin/python

import sys
import os

def get_sha(argv):
	sha = []
	with open(argv) as f:
		for line in f:
			if "commit " in line:
				commit = line.split()[1]
				if len(commit) == 40:
					sha.insert(0, commit)
	return sha

def write_sha(sha, argv2):
	file_name = argv2 + '_sha.txt'
	f = open(file_name, 'w')
	for x in sha:
		f.write(x+'\n')  
	f.close()  

def main(argv1, argv2):
	write_sha(get_sha(argv1), argv2)

if __name__ == "__main__":
   main(sys.argv[1], sys.argv[2])
