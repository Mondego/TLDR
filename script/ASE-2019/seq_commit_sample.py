#!/usr/bin/python

import sys
import os
import random
import git 
import math

sha = []
all_git_log = {
				'/Users/demigorgan/TLDR_EXP/COMMIT_LOG/io.txt', 
				
				}

def get_sha(argv):
	commit = []
	with open(argv) as f:
		for line in f:
			if "commit " in line:
				commit.append(line.split()[1])
	return commit

def sample(argv):
	filename = 'Sampled_Commit/'+ argv[argv.rindex('/')+1:]
	sha = get_sha(argv)
	
	#### watch out while changing it....the less the end the more recent we start
	end = 700
	with open(filename, 'w') as f:
		for i in range(1, 30):
			f.write(sha[end - i]+"\n")
	
if __name__ == "__main__":
   for log in all_git_log:
	   sample(log)
