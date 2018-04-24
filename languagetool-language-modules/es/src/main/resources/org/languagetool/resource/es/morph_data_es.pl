#!/usr/bin/perl
eval 'exec /usr/bin/perl -S $0 ${1+"$@"}'
    if $running_under_some_shell;
			# this emulates #! processing on NIH machines.
			# (remove #! line above if indigestible)

eval '$'.$1.'$2;' while $ARGV[0] =~ /^([A-Za-z_0-9]+=)(.*)/ && shift;
			# process any FOO=bar switches

# This script coverts data in the format:
# inflected_formHTlexemeHTtags
# (where HT is the horizontal tabulation)
# to the form:
# inflected_form+Kending+tags
# where '_' is a separator, K is a character that specifies how many characters
# should be deleted from the end of the inflected form to produce the lexeme
# by concatenated the stripped string with the ending.
#
# Written by Jan Daciuk <jandac@pg.gda.pl>, 1997
#

$separator = '*';

while (<>) {
    chop;	# strip record separator
    @Fld = split('\t', $_, 9999);

    $l1 = length($Fld[0]);
    if (($prefix = &common_prefix($Fld[0], $Fld[1], $l1))) {
	printf '%s%s%c%s%s%s', $Fld[0], $separator, 
	($l1 - $prefix + 65), substr($Fld[1], $prefix, 999999), 
	$separator, $Fld[2];
    }
    else {
	printf '%s%s%c%s%s%s', $Fld[0], $separator, 65 + $l1, $Fld[1],

	  $separator, $Fld[2];
    }
    # Delete the following (1) line if your tags do not contain spaces
    # and you would like to append comments at the end of lines
    for ($i = 3; $i < $#Fld; $i++) {
	printf ' %s', $Fld[$i];
	# Do not delete this
	;
    }
    printf "\n";
}

sub common_prefix {
    local($s1, $s2, $n, $i) = @_;
    for ($i = 0; $i < $n; $i++) {
	if (substr($s1, $i, 1) ne substr($s2, $i, 1)) {	#???
	    return $i;
	}
    }
    $n;
}
