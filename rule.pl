#!/usr/bin/perl
use warnings;
use strict;
use utf8;

binmode(STDOUT,":utf8");

my @things = qw/
SEISEAN;seisean;eisean
SI;sí;í
SISE;sise;ise
SIAD;siad;iad
SIADSAN;siadsan;iadsan
/;

for my $th (@things) {
    my ($a, $b, $c) = split/;/, $th;

print<<__END__;
        <rule id="NOTVERB_$a" name="NOTVERB $b">
            <pattern>
                <token postag=".*Verb.*" postag_regexp="yes" negate_pos="yes"></token>
                <marker>
                    <token postag=".*Pron.*" postag_regexp="yes">$b</token>
                </marker>
            </pattern>
            <message>Ba chóir duit <suggestion>$c</suggestion> a scríobh.</message>
            <example correction="$c">is fearr liom <marker>$b</marker></example>
        </rule>
        <rule id="COP_$a" name="NOTVERB $b">
            <pattern>
                <token postag=".*Cop.*" postag_regexp="yes"></token>
                <marker>
                    <token postag=".*Pron.*" postag_regexp="yes">$b</token>
                </marker>
            </pattern>
            <message>Ba chóir duit <suggestion>$c</suggestion> a scríobh.</message>
            <example correction="$c">ba <marker>$b</marker></example>
        </rule>
        <rule id="AUT_$a" name="NOTVERB $b">
            <pattern>
                <token postag=".*Verb.*Auto.*" postag_regexp="yes"></token>
                <marker>
                    <token postag=".*Pron.*" postag_regexp="yes">$b</token>
                </marker>
            </pattern>
            <message>Ba chóir duit <suggestion>$c</suggestion> a scríobh.</message>
            <example correction="$c">deirtear <marker>$b</marker></example>
        </rule>
__END__
}