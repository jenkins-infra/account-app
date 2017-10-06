#!/usr/bin/env python
#Python 2.7.13
import random
import md5
import uuid
import sys
import os

# This script generate fake election data for testing purposes.

elections = [
        '19931123',
        '20031124',
        '20131123',
        ]

candidates = [
        'bob',
        'alice',
        'john',
        'paul',
        'jack',
        'tom'
        ]

for election in elections:
    dirname = os.getcwd() + "/" + 'fake_data' + "/" + election + "/"
    directory = os.path.dirname(dirname)

    if not os.path.exists(directory):
        os.makedirs(directory)
        os.chmod(directory,0777)

    for i in range(100):
        vote=[]
        c = list(candidates)
        file = open( os.path.join( dirname, uuid.uuid4().hex + ".csv"),"w" )
        while (len(c) > 0):
            secure_random = random.SystemRandom()
            selected = secure_random.choice(c)
            c.remove(selected)
            vote.append(selected)
            file.write(selected)
            if len(c) > 0:
                file.write(",")
            else:
                file.write("\n")
        file.close

print "Fake data created"

