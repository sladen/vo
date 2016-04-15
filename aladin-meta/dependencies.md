CDS Aladin dependency notes
===========================

```shell
$ unzip -l AladinSrc_9.013.jar | grep .jar
Archive:  AladinSrc_9.013.jar
    41538  2015-12-14 08:29   Moc.jar
   112245  2014-11-10 16:02   cds.astro.jar
     1780  2014-11-10 16:02   cds.savot.common.jar
    39452  2014-11-10 16:02   cds.savot.model.jar
    19939  2014-11-10 16:02   cds.savot.pull.jar
    11430  2014-11-10 16:02   cds.savot.sax.jar
    17044  2014-11-10 16:02   cds.savot.writer.jar
   712822  2015-12-07 12:15   jsamp.jar
    43858  2014-11-10 16:02   kxml2-2.3.0.jar
    97452  2014-11-10 16:02   microhub.jar
```

## Multi-Order Coverage 
  * `cds.moc`
  * http://ivoa.net/documents/MOC/
  * http://wiki.ivoa.net/twiki/bin/view/IVOA/MocInfo
  * http://wiki.ivoa.net/internal/IVOA/MocInfo/MocSrc.jar =4.5

## CDS Astro
  * `cds.astro.Udef`, `cds.astro.Qbox`
  * http://cds.u-strasbg.fr/resources/doku.php?id=downloads
  * http://cds.u-strasbg.fr/resources/doku.php?id=units (Homepage)
  * http://cds.u-strasbg.fr/resources/doku.php?id=using_conditions (Licence)
  * http://cdsarc.u-strasbg.fr/doc/javadoc/cds/astro/package-summary.html
  * http://cds.u-strasbg.fr/cdsdevcorner/units1.2/src.zip =1.2
  * Non-Free licence, restricted to educational use
  * author Francois Ochsenbein -- francois@astro.u-strasbg.fr

(2006-11, but contains filed dated February 2007)

## CDS Simple Access to VO Tables (SAVOT)
  * `cds.savot`
  * https://github.com/aschaaff/savot/ =4
  * authors: Andre Schaaff (CDS), Laurent Bourges (JMMC)
  * GPL

## jsamp
  * `org.astrogrid.samp`
  * https://github.com/mbtaylor/jsamp
  * RFP: https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=680475
  * https://lists.debian.org/debian-java/2013/05/msg00185.html
  * http://anonscm.debian.org/cgit/debian-science/packages/jsamp.git

## kxml2-2.3.0.jar
  * http://www.kxml.org/
  * https://packages.debian.org/search?keywords=libkxml2-java =2.3.0+ds1-2

## microhub
```shell
$ unzip -l tmp/microhub.jar | awk '/class/{print $4}' | xargs -n1 dirname | sort -u
net/ladypleaser/rmilite
net/ladypleaser/rmilite/impl
org/apache/commons/codec
org/apache/commons/codec/binary
org/apache/xmlrpc
org/apache/xmlrpc/util
org/votech/plastic
org/xml/sax
uk/ac/starlink/plastic
uk/co/wilson/xml
uk/org/xml/sax
```

### RMI-Lite
  * `net.ladypleaser.rmilite`
  * https://sourceforge.net/projects/rmi-lite/
  * https://github.com/cowboyd/rmi-lite
"Last Update: 2013-04-08"

### Apache Commons Codec 1.10
  * `org.apache.commons.codec`
  * Apache 2.0 licence
  * https://commons.apache.org/proper/commons-codec/
  * https://commons.apache.org/proper/commons-codec/download_codec.cgi
  * http://apache.mirror.anlx.net//commons/codec/source/commons-codec-1.10-src.tar.gz
  * https://packages.debian.org/search?keywords=libcommons-codec-java =1.10-1

### Plastic
  * `org.votech.plastic`
  * http://www.star.bris.ac.uk/~mbt/plastic/javadocs/org/votech/plastic/package-tree.html
  * https://github.com/Starlink/starjava/tree/master/plastic
  * https://github.com/Starlink/starjava/tree/master/plastic/src/main/org/votech/plastic
  * https://github.com/Starlink/starjava/tree/master/plastic/src/main/uk/ac/starlink/plastic
`cds/aladin/Aladin.java`:
```java
   protected static boolean USE_PLASTIC_REQUESTED = false;
   protected static boolean USE_SAMP_REQUESTED = false;
…
      if( appMessagingMgr==null ) {
         // choice at user request ?
         if( USE_SAMP_REQUESTED ) {
            appMessagingMgr = new SAMPManager(this);
         }
         else if( USE_PLASTIC_REQUESTED ) {
            appMessagingMgr = new PlasticManager(this);
         }
```
Can probably be ripped out.

### Sax
  * `org.xml.sax`
  * https://packages.debian.org/search?keywords=libcrimson-java =1:1.1.3-11

### UK Sax
  * `uk.org.xml.sax`
  * https://ws.apache.org/xmlrpc/xmlrpc2/apidocs/uk/org/xml/sax/package-summary.html
  * http://web.archive.org/web/20071009081423/http://xml.org.uk/index.htm "Welcome to XML UK's alternative site. The main site is on http://www.xmluk.org/ ."
  * http://web.archive.org/web/20071009193544/http://www.xmluk.org/ "XML UK is a recognised Affiliate Group of the British Computer Society. "

### Wilson XML
  * https://ws.apache.org/xmlrpc/xmlrpc2/apidocs/uk/co/wilson/xml/package-summary.html
  * http://grepcode.com/file/repo1.maven.org/maven2/xmlrpc/xmlrpc/2.0.1/uk/co/wilson/xml/MinML.java

