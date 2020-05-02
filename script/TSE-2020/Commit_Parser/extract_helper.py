#!/usr/bin/python

import sys
import os

# This method parses git log history, Extracts the hashcodes for each commit, 
# and list the latest 40 of them by oldest to latest order.
def get_sha(argv):
	sha = []
	with open(argv) as f:
		for line in f:
			if "commit " in line:
				commit = line.split()[1]
				if len(commit) == 40:
					sha.insert(0,commit)
	length = len(sha)
	return sha[length - 40: length]

def write_sha(sha, argv1):
	os.unlink(argv1)
	file_name = argv1
	f = open(file_name, 'w')
	for x in sha:
		f.write(x+'\n')  
	f.close()  

if __name__ == "__main__":
   write_sha(get_sha(sys.argv[1]), sys.argv[1])