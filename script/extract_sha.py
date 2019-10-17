#!/usr/bin/python

import sys
import os
import git 


def get_sha(argv):
	sha = []
	with open(argv) as f:
		for line in f:
			if "commit " in line:
				commit = line.split()[1]
				if len(commit) == 40:
					sha.insert(0, commit)
	return sha

def write_sha(sha):
	f = open('sha.txt', 'w')
	for x in sha:
		f.write(x+'\n')  
	f.close()  

def main(argv):
	write_sha(get_sha(argv))

if __name__ == "__main__":
   main(sys.argv[1])
