#!/usr/bin/perl
use strict;
use warnings;
no warnings 'uninitialized';
use utf8;

use open qw(:std :utf8);
binmode STDOUT, ':utf8';

my %rule_count;

my @rule_nums;
my @rule_ids;
my @rule_subids;
my @messages;
my @underlines;
my @contexts;
my @suggestions;
my @moreinfos;

my $previousline = "";
my $rule_num=0;
my $rule_id="";
my $rule_subid=0;
my $message = "";
my $underline = "";
my $context = "";
my $suggestion = "";
my $moreinfo = "";

my $unkownwords = "";

while (<>) {
    chomp();
    # rule number and ID
    if (/^(\d+)\.\) Line .+ column .+ Rule ID: ([^[]+)(\[(\d+)\])?$/) {
	&guardaRegla();
	$rule_num=$1;
	$rule_id=$2;
	if (length $4) {
	    $rule_subid=$4;
	} 
    }
    # message
    if (/^Message: (.+)$/) {
	$message = $1;
    }
    # suggestions
    if (/^Suggestion: (.+)$/) {
	$suggestion = $1;
    }
    # more info
    if (/^More info: (.+)$/) {
	$moreinfo = $1;
    }
    # underline and context
    if (/^(\s*)([\^]+)/) {
	$underline = $_;
	$context = $previousline;
	#print "$rule_num $rule_id $rule_subid $message\n$context\n$underline\n";
    }
    if (/^Unknown words: \[(.+)\]$/) {
	$unkownwords = $1;
    }
    $previousline = $_;
}
&guardaRegla();

my $totalmatches = scalar @rule_ids;
my $menu="<hr/>Vés a: [<a href=\"#inici\">Inici</a>] [<a href=\"#indexregles\">Índex de regles</a>]<hr/>\n";

print "<html>\n<head><meta charset=\"UTF-8\"></head>\n<body>\n";
print "<h1><a name=\"inici\">Resultats de la revisió</a></h1>\n";
print $menu;
print "<h2>Paraules desconegudes</h2>\n";
print "<p>".$unkownwords."</p>\n";
print "<a name=\"indexregles\"></a>".$menu;
print "<h2>Regles ordenades per freqüència</h2>\n";

print "<table border=\"1\">";
print "<tr><td><b>Codi de regla</b></td><td><b>Ocurrències</b></td></tr>";
foreach my $key  (sort { $rule_count{$b} <=> $rule_count{$a} } keys %rule_count)  {
    print "<tr><td><a href=\"#$key\">$key</a></td><td style=\"text-align:right\">$rule_count{$key}</td></tr>\n";
}
    print "<tr><td style=\"text-align:right\">Total:&nbsp;</td><td style=\"text-align:right\">$totalmatches</td></tr>\n";
print "</table>";

print "<h2>Errors trobats per regla</h2>\n";
foreach my $key (sort { $rule_count{$b} <=> $rule_count{$a} } keys %rule_count)  {
    print "<a name=\"$key\"></a>".$menu."<br/>";
    print "*** Regla: <b>$key</b> ***";
    print "<br/><br/>";
    for (my $i=0; $i<$totalmatches; $i++) {
	if ($rule_ids[$i] =~ /^$key$/) {
	    print "Missatge: ".$messages[$i]."<br/>\n";
	    print "Suggeriments: ".$suggestions[$i]."<br/>\n";
	    if (length $moreinfos[$i]) {
		print "<a href=\"$moreinfos[$i]\">Més informació</a><br/>\n";
	    }
	    print "<pre>";
	    print $contexts[$i]."\n";
	    print $underlines[$i]."\n";
	    print "</pre>\n";
	}
    }
    
}



print "</body>\n</html>";


sub guardaRegla {

    if (length $rule_id) {
	if (exists $rule_count{$rule_id}) {
	    $rule_count{$rule_id}++;
	} else {
	    $rule_count{$rule_id} = 1;
	}
	
	push(@rule_nums, $rule_num);
	push(@rule_ids, $rule_id);
	push(@rule_subids, $rule_subid);
	push(@messages, $message);
	push(@underlines, $underline);
	push(@contexts, $context);
	push(@suggestions, $suggestion);
	push(@moreinfos, $moreinfo);

	$rule_num=0;
	$rule_id="";
	$rule_subid=0;
	$message = "";
	$underline = "";
	$context = "";
	$suggestion = "";
	$moreinfo = "";
    }

}
