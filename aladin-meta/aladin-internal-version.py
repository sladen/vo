#!/usr/bin/env python

import sys
import re

def main(filename='cds/aladin/Aladin.java'):
    success = 0
    VERSION = 'String VERSION.*"v(\d\.\d+)".*;'
    lines = open(sys.argv[1], 'r').readlines()
    for l in lines:
        m = re.search(VERSION, l)
        if m:
            print m.group(1)
            success += 1

    # We want one match of the version string, and only one match.
    sys.exit(success != 1)

if __name__=='__main__':
    main()
    
