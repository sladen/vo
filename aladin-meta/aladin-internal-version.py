#!/usr/bin/env python

import sys
import re
import zipfile

def hunt_versions(lines):
    VERSION = 'String VERSION.*"v(\d\.\d+)".*;'
    results = []
    for l in lines:
        m = re.search(VERSION, l)
        if m:
            results.append(m.group(1))
    return results

def dump_java_file(filename, internal_path = 'cds/aladin/Aladin.java'):
    try:
        # Open local jar (zip) file and look inside
        srcjar = zipfile.ZipFile(filename, 'r')
        f = srcjar.open(internal_path)
    except zipfile.BadZipfile as e:
        # Open java file directly
        f = open(filename, 'r')
    lines = f.readlines()
    return lines

def main(filename):
    lines = dump_java_file(filename)
    versions = hunt_versions(lines)

    if len(versions) >= 1:
        sys.stdout.write(versions[0] + '\n')

    # Report external success only if there was a *single* unambigious
    # version number detected, no more, no fewer
    sys.exit(len(versions) != 1)

if __name__=='__main__':
    print sys.argv[1:]
    list(map(main, sys.argv[1:]))
    
