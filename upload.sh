#!/bin/sh
#
#

if [ -z $1 ] ; then
  echo "./upload.sh <release-no>"
else
  STOREDIR="milestones"
  if [ "$2" == release ] ; then
    STOREDIR="products"
  fi

  cd $HOME/.dpml/data/cache
  FILES=`find pax/logging -name "*$1*" -type f | grep -v alpha | grep -v beta`
  tar cvfz $HOME/repo.tgz $FILES

  scp $HOME/repo.tgz repository.ops4j.org:.

  rm $HOME/repo.tgz

  ssh repository.ops4j.org "cd /var/http/repository.ops4j.org/classic ; tar xvfz $HOME/repo.tgz ; rm $HOME/repo.tgz"
fi