#!/usr/bin/python

import sys
import os
import git 


def get_sha(argv):
	sha = []
	with open(argv) as f:
		for line in f:
			if "commit " in line:
				sha.append(line.split()[1])
	return sha

def write_sha(sha):
	f = open('sha.txt', 'w')
	for x in sha:
		f.write(x+'\n')  
	f.close()  

def main(argv):
	write_sha(get_sha(argv))
	#repo = git.Repo('https://github.com/apache/commons-configuration.git')
	#repo.remotes.origin.pull()


if __name__ == "__main__":
   main(sys.argv[1])
