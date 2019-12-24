#!/usr/bin/perl
use warnings;
use strict;
use utf8;

binmode(STDIN, ":utf8");
binmode(STDOUT, ":utf8");

my $reading = 0;
while(<>) {
    chomp;
    if(/^( +)<message>Foirm neamhchaighdeánach de/) {
        my $sp = $1;
        print "$_\n";
        print "$sp<short>Neamhchaighdeánach</short>\n";
        next;
    }
    if(/^( +)<message>Ní úsáidtear an focal/) {
        my $sp = $1;
        print "$_\n";
        print "$sp<short>In abairt</short>\n";
        next;
    }
    if(/^( +)<message>Focal ceart ach tá/) {
        my $sp = $1;
        print "$_\n";
        print "$sp<short>Neamhchoitianta</short>\n";
        next;
    }
    print "$_\n";
}
