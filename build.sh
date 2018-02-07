#!/bin/bash

MAIN_FILE=index
MAIN_SOURCE_EXTENSION=md
BIBFILE=mybibentries.bib
CSS_FILE=pandoc.css


pandoc -s -o $MAIN_FILE.html src/$MAIN_FILE.$MAIN_SOURCE_EXTENSION --bibliography src/$BIBFILE --css themes/$CSS_FILE 

cd src/utils
javac BibFixer.java
java BibFixer ../../$MAIN_FILE.html

