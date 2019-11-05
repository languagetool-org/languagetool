#!/usr/bin/perl

use warnings;
use strict;
use utf8;

binmode(STDOUT, ":utf8");

sub unfada {
    my $in = shift;
    $in =~ s/á/a/g;
    $in =~ s/é/e/g;
    $in =~ s/í/i/g;
    $in =~ s/ó/o/g;
    $in =~ s/ú/u/g;
    return $in;
}
for my $word (qw/inar lenar má murar nár níor ó ónar sular trínar/) {
    my $upper = uc(unfada($word));
my $out=<<__END__;
        <rule id="${upper}_VNOPAUT" name="$word cantar">
            <pattern>
                <token>$word</token>
                <marker>
                    <token postag=".*Verb.*" postag_regexp="yes"><exception postag=".*:Len" postag_regexp="yes"/><exception postag=".*:PastInd.*:Auto.*" postag_regexp="yes"/></token>
                </marker>
            </pattern>
            <message>Séimhiú ar iarraidh: <suggestion><match no="2" postag="([^:]*):(.*)" postag_regexp="yes" postag_replace="\$1:\$2:Len"/></suggestion></message>
            <example correction='chantar'>$word <marker>cantar</marker></example>
            <example>$word canadh</example>
        </rule>
__END__
    print $out;
}