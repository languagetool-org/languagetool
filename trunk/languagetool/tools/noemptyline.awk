#
#noemptyline.awk
#used by TKLspell (posi.sh)
#
  { if (length($0) < 3) next; 
    print $0;
  }
