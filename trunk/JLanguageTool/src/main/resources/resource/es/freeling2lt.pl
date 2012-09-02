#!/usr/bin/perl

# Convert the Freeling dictionary into LanguageTools format
# sdocio@gmail.com
# Usage: cat dicc.src | ./freeling2lt.pl > language.dict

while(<>)
{
  chomp;

  @tokens = split;

  if( $#tokens > 2 )
  {
    my $wordform = shift(@tokens);
    for(my $i=0; $i<$#tokens; $i+=2)
    {
      print "$wordform\t$tokens[$i]\t$tokens[$i+1]\n";
    }
  }
  else
  {
    $" = "\t";
    print "@tokens\n";
  }
}
