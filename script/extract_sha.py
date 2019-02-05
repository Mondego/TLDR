#!/usr/bin/python

import sys
import os
import git 

all_commits = 'repo_log.txt'

def get_sha():
	sha = []
	with open(all_commits) as f:
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
	write_sha(get_sha())
	#repo = git.Repo('https://github.com/apache/commons-configuration.git')
	#repo.remotes.origin.pull()


if __name__ == "__main__":
   main(sys.argv[1])
