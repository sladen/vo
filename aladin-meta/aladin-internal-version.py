#!/usr/bin/env python

import sys
import re
import zipfile

def hunt_versions(lines):
    VERSION = 'String VERSION.*"v(\d+\.\d+)".*;'
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
    except (KeyError, IOError) as e:
        # IOError: Exists Not a ZIP/JAR file (ie. a directory)
        # KeyError: JAR file, but 'Aladin.java' couldn't be found
        return ''
    except zipfile.BadZipfile as e:
        # Open java file directly
        f = open(filename, 'r')
    lines = f.readlines()
    return lines

def versions_from_filename(filename):
    lines = dump_java_file(filename)
    versions = hunt_versions(lines)
    return versions

def main():
    complete_success = True
    for filename in sys.argv[1:]:
        versions = versions_from_filename(filename)

        # We are only happy if we fine a single version for each JAR file
        if len(versions) != 1:
            complete_success = False
        if len(versions) >= 1:
            sys.stdout.write('%s "%s"\n' % (versions[0], filename))

    # Report external success only if there was a *single* unambigious
    # version number detected, no more, no fewer
    sys.exit(not complete_success)

if __name__=='__main__':
    main()
    print sys.argv[1:]
