# checklanguagetool checks the given languagetool directory
#
E_NODIR1=66
if [ ! -d $1 ]
then
   echo "bad"
   exit $E_NODIR1
fi
if [ ! -r $1/TextChecker.py ]
then
   echo "bad1"
   exit $E_NODIR1
fi
if [ ! -d $1/data ]
then
   echo "bad2"
   exit $E_NODIR1
fi
if [ ! -d $1/src ]
then
   echo "bad3"
   exit $E_NODIR1
fi
if [ ! -d $1/tools ]
then
   echo "bad4"
   exit $E_NODIR1
fi
if [ ! -r $1/tools/posi.sh  ]
then
   echo "bad5"
   exit $E_NODIR1
fi
if [ ! -r $1/tools/posi.awk ]
then
   echo "bad6"
   exit $E_NODIR1
fi
