#noemptyline.awk
  { if (length($0) < 3) next; 
    print $0;
  }
