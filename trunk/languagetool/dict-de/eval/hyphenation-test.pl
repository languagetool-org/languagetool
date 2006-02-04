#!/usr/bin/perl -w
# (C) 2006 Daniel Naber (www.danielnaber.de), 2006-02-03
# Runs hyphenation using altLinuxHyph's "example" command on
# a set of pre-hyphenated words and compares the result to
# the expected result.

# Configure these:
$command = "/home/dnaber/prg/altlinuxHyph/example";
$dic_file = "/home/dnaber/prg/altlinuxHyph/de_fixed3.dic";
$tmp_file = "/tmp/hyph-test.txt";

if (!$ARGV[0]) {
	print "Usage: ./hyphenation-test.pl <file>\n";
	exit;
}
main();

sub main {
	@words = load_words($ARGV[0]);
	foreach $word (@words) {
		open(OUT, ">$tmp_file") || die "Cannot open: $tmp_file: $!";
		$word_to_hyphenate = $word;
		$word_to_hyphenate =~ s/-//g;
		print OUT $word_to_hyphenate."\n";
		close(OUT);
		$output = `$command $dic_file $tmp_file`;
		@expected_hyph_points = get_hyph_points($word);
		@found_hyph_points = get_hyph_points($output);
 		#print $output;
		#print "EXP  =".join(',', @expected_hyph_points)."\n";
		#print "FOUND=".join(',', @found_hyph_points)."\n";
		@missing_points = ();
		for $expected_point (@expected_hyph_points) {
			if (!grep(/$expected_point/, @found_hyph_points)) {
				push(@missing_points, $expected_point);
			}
		}
		@wrong_points = ();
		for $found_point (@found_hyph_points) {
			if (!grep(/$found_point/, @expected_hyph_points)) {
				push(@wrong_points, $found_point);
			}
		}
		if (scalar(@wrong_points) > 0) {
			print "Wrong hyphenation: expected: $word, got: $output";
		}
		#if (length(@missing_points) > 0) {
		#	print "Missing hyphenation: expected: $word, got: $output";
		#}
	}
}

sub load_words {
	my $filename = shift;
	open(INP, $filename) || die "Cannot open '$filename': $!";
	@words = ();
	while ($line = <INP>) {
		$line =~ s/#.*//;		# remove comments
		$line =~ s/^\s+//;
		$line =~ s/\s+$//;
		push(@words, $line);
	}
	close(INP);
	return @words;
}

sub get_hyph_points {
	my $word = shift;
	my @points = ();
	my $pos = 0;
	for ($i = 0; $i < length($word); $i++) {
		#print "*".substr($word, $i, 1);
		if (substr($word, $i, 1) eq "-") {
			push(@points, $pos);
		} else {
			$pos++;
		}
	}
	return @points;
}
