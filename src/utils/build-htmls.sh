#!/bin/bash

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

#pandoc -s -o index.html index.adoc --bibliography mybibentries.bib --css pandoc.css
#pandoc -s -o $SCRIPTPATH/../../index.html $SCRIPTPATH/../index.md --css $SCRIPTPATH/../theme/pandoc.css
pandoc -s -o ../index.html --css themes/pandoc.css index.md 
