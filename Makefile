PROJ = midpsmtp
VERSION = 0.3.0

TOOLS=\
    bin/run.sh 
    

MANIFEST = bin/MANIFEST.MF
    
JADFILE = bin/$(PROJ).jad
JARFILE = bin/$(PROJ).jar

SOURCES = \
    src/Base64.java \
    src/OptionsForm.java \
    src/Settings.java \
    src/SMTPClient.java \
    src/SMTPException.java \
    src/SMTPSocket.java \
    src/Throbber.java \
    src/MailAddress.java

LIB_DIR=../../lib
CLDCAPI=${LIB_DIR}/cldcapi10.jar
MIDPAPI=${LIB_DIR}/midpapi20.jar
PREVERIFY=../../bin/preverify
FIXJAD=bin/fixjad.pl

PATHSEP=":"

JAVAC=javac
JAR=jar


$(JARFILE):  $(SOURCES) $(JADFILE) $(MANIFEST)
	mkdir -p classes
	mkdir -p tmpclasses

	${JAVAC} \
	    -bootclasspath ${CLDCAPI}${PATHSEP}${MIDPAPI} \
	    -d tmpclasses \
	    -classpath tmpclasses \
	    `find ./src -name '*'.java`

	${PREVERIFY} \
	    -classpath ${CLDCAPI}${PATHSEP}${MIDPAPI}${PATHSEP}../tmpclasses \
	    -d classes \
	    tmpclasses

	echo "Jaring preverified class files..."
	$(JAR) cmf $(MANIFEST) $(JARFILE) -C classes . -C res .
	$(FIXJAD) $(JARFILE) $(JADFILE)

clean:	
	if [ -d classes ]; then rm -rf classes; fi
	if [ -d tmpclasses ]; then rm -rf tmpclasses; fi
	rm -f `find -name '*~'`

distclean: clean
	if [ -f $(JARFILE) ]; then rm -f $(JARFILE) ;fi

dist:
	tar -cjf $(PROJ)-$(VERSION).tar.bz2 \
	    `find ./src/ -name '*.java'` \
	    $(TOOLS) res $(JADFILE) \
	    project.properties \
	    README \
	    Makefile
	
bdist:	clean $(JARFILE)
	cp $(JARFILE) $(PROJ)-$(VERSION).jar
	cp $(JADFILE) $(PROJ)-$(VERSION).jad
	bin/fixjad.pl $(PROJ)-$(VERSION).jar $(PROJ)-$(VERSION).jad

alldist: dist bdist
	