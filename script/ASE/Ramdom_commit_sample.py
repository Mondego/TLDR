#!/usr/bin/python

import sys
import os
import random
import git 
import math

sha = []
all_git_log = {
				'/Users/demigorgan/TLDR_EXP/COMMIT_LOG/math.txt', 
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

	step = int(math.ceil(len(sha) / 40))
	end = int(len(sha))

	with open(filename, 'w') as f:
		for i in range(1, 50):
			#rand = random.randint(end - step, end)
			rand = random.randint(end - step, end)
			end = end - step
			f.write(sha[rand % len(sha)]+"\n")
			if rand > 0:
				f.write(sha[abs(rand - 1) % len(sha)]+"\n") 
			
			#end = abs(end - step - 1)
	
if __name__ == "__main__":
   for log in all_git_log:
	   sample(log)
