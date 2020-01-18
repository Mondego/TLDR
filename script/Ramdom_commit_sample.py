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

	step = int(math.ceil(len(sha) / 44))
	#step = 1
	end = int(len(sha) / 2)
	with open(filename, 'w') as f:
		for i in range(1, 24):
			rand = random.randint(end - step, end)
			f.write(sha[rand % len(sha)]+"\n")
			if rand > 0:
				f.write(sha[abs(rand - 1) % len(sha)]+"\n") 
			end = abs(end - step - 1)
	
if __name__ == "__main__":
   for log in all_git_log:
	   sample(log)
