#!/bin/bash

if [ "$1" == "--help" -o "$1" == "-h" ] ; then
  echo "Command line parameters:"
  echo " 1: bibfile"
  echo " 2: target text file"
  exit 0
fi

while read P; do
  case $P in @*)
    WITH_COMMA=${P##*\{}
    BIBID=${WITH_COMMA%\,*}
    echo "[@$BIBID]" >> $2
  esac
done <$1

