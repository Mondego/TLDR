#!/usr/bin/python

import sys
import os
import random
import git 
import math

sha = []
all_git_log = {'/Users/demigorgan/TLDR_EXP/COMMIT_LOG/collection.txt', 
				'/Users/demigorgan/TLDR_EXP/COMMIT_LOG/io.txt', 
				'/Users/demigorgan/TLDR_EXP/COMMIT_LOG/jxpath.txt',	
				'/Users/demigorgan/TLDR_EXP/COMMIT_LOG/net.txt',	
				'/Users/demigorgan/TLDR_EXP/COMMIT_LOG/validator.txt'}

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

	step = int(math.ceil(len(sha) / 11))
	end = len(sha) - 1
	with open(filename, 'w') as f:
		for i in range(1, 11):
			rand = random.randint(end - step, end)
			f.write(sha[rand % len(sha)]+"\n") 
			end = end - step - 1
	
if __name__ == "__main__":
   for log in all_git_log:
	   sample(log)
