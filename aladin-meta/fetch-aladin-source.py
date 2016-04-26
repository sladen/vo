#!/usr/bin/env python
# Paul Sladen, 2016, Astronomisches Rechen-Institut/GAVO, GPLv2+
# Aladin is kept in revision control, but the repository is not published
# Aladin is published externally in unversioned .jar tarballs.
# This script tries to obtain and rename the latest source versions
# to enable re-import + allow tagging into revision control (.git)
# The version number can be obtained from the download page HTML
# The HTML doesn't validate easily, so must be parsed as soup
# The returned Java source also contents several binary .class files
# When unpacked jarballs have no top-level 'Aladin' directory
#
# Manual updates mean that the source and HTML version get out-of-step
# (Possible to skip based on Last-modified date of download page?)
# Kudos to Pierre Fernique for a heads-up that the version number is
# available as 'vN.nnn - mth. month day HH:mm:ss CET YYYY' from:
# http://aladin.unistra.fr/java/VERSION
#
# The HTML5-based AladinLite (HTML5-based) is distributed separately
# There is a first-level directory which contains the release version/date
# wget http://aladin.u-strasbg.fr/AladinLite/api/v2/latest/AladinLiteSrc.tar.gz
# $ tar ztf AladinLiteSrc.tar.gz | grep -m1 AladinLite-
# ./AladinLite-2015-12-16/

import urllib
import urllib2
import urlparse
import BeautifulSoup
import os.path
import time
import sys
import rfc822

def desktop_source_version(soup, branch = 'Official', base_url = None, limit = 12):
    official = soup.find(name='a', attrs={'name': branch})
    heading = official.findNext(['h1', 'h3'])
    font = heading.find('font')
    # version '9.010' did not include the prepended 'v'
    scraped_version = font.contents[0].split(' ')[0].lstrip('v')
    # validate it's a valid number: but don't convert,
    # otherwise trailing zeros are lost. (eg. 1.230)
    float(scraped_version)
    links = font.findAllNext(name='a', limit=12)
    for a in links:
        if 'Src' in a.get('href'):
            scraped_source_jar = a.get('href')
            break
    return scraped_source_jar, scraped_version

def fetch_rename(url, version, base = None, twidder = None):
    if base is not None:
        url = urllib.basejoin(base, url)
    path = urlparse.urlparse(url).path
    filename = os.path.basename(path)
    base, ext = os.path.splitext(filename)
    destination = "%s_%s%s" % (base, version, ext)

    def progress_ticker(blocks, blocksize, total):
        BACKSPACE = '\x08'
        percentage = "{0:6.2f}%".format(min(100.0,blocks*blocksize*100.0/total))
        sys.stdout.write(BACKSPACE * len(percentage) + percentage)

    progress = progress_ticker
    sys.stdout.write('%9s %s %s %s' % (' ', version, destination, url))
    # Percentage update on the console, looks nice if console wide-enough
    if twidder is True or (twidder is None and sys.stdout.isatty()):
        sys.stdout.write('\r')
    else: progress = None

    # Parse out Content-Length
    def get_length(headers):
        length = int(headers['Content-Length'])
        return length
    
    # Parse out Last-Modified date in a reliable way
    def get_seconds(headers):
        date_tuple = headers.getdate_tz('Last-Modified')
        epoch_seconds = rfc822.mktime_tz(date_tuple)
        return epoch_seconds

    # Perform a HEAD request
    def date_size_via_head(url):
        request = urllib2.Request(url)
        request.get_method = lambda: 'HEAD'
        conn = urllib2.urlopen(request)
        return get_seconds(conn.headers), get_length(conn.headers)

    # Don't clobber existing local files
    if os.path.isfile(destination):
        remote_mtime, remote_size = date_size_via_head(url)
        local_mtime = os.path.getmtime(destination)
        local_size = os.path.getsize(destination)
        sys.stdout.write('%8s' % '[skipped]')
        if remote_size != local_size:
            sys.stderr.write('Warning: existing "%s" has size mismatch (incomplete cached copy?)\n' % destination)
        if remote_mtime != local_mtime:
            sys.stderr.write('Warning: existing "%s" has timestamp mismatch (out-of-date cached copy?)\n' % destination)
        sys.stdout.write('\n')
    else:
        local_filename, headers = urllib.urlretrieve(url, destination, progress)

        # Need to use _tz; plain time.mktime() would lose the accurate timezone
        epoch_seconds = get_seconds(headers)
        # reset (atime + mtime) at the same time
        os.utime(local_filename, (epoch_seconds,) * 2)
        sys.stdout.write('\n')

def download_desktop_source():
    download_page_url = 'http://aladin.u-strasbg.fr/java/nph-aladin.pl?frame=downloading'

    request = urllib2.Request(download_page_url)
    response = urllib2.urlopen(request)

    # Non-compliant HTML, so need to use forgiving HTML parser
    html = response.read()
    soup = BeautifulSoup.BeautifulSoup(html)

    # The two Java targets we're interested in
    for java_branch in 'Official', 'Beta':
        path, version = desktop_source_version(soup, java_branch)
        fetch_rename(path, version, base = response.geturl())

def download_lite_source():
    """Still needs to actively inspect the first entry in the tarball
    to obtain the version number
    """
    download_page_url = 'http://aladin.u-strasbg.fr/AladinLite/doc/#source-code'

    def html5_source(soup, limit = 12):
        official = soup.find(name='a', attrs={'name': 'source-code'})
        a = official.findNext('a')
        if 'Src' in a.get('href'):
            scraped_source_tar = a.get('href')
        return scraped_source_tar

    request = urllib2.Request(download_page_url)
    response = urllib2.urlopen(request)
    soup = BeautifulSoup.BeautifulSoup(response.read())

    path = html5_source(soup)
    url = urllib.basejoin(response.geturl(), path)

    request = urllib2.Request(download_page_url)

def main():
    #download_lite_source()
    download_desktop_source()

if __name__=='__main__':
    main()
