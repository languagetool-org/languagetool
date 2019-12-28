#!/usr/bin/perl
use warnings;
use strict;
use utf8;

binmode(STDOUT,":utf8");

my @things = qw/
DO;n?[Dd]h?ó;dó;dhá
/;

for my $th (@things) {
    my ($a, $b, $c, $d) = split/;/, $th;

print<<__END__;
        <rulegroup id="${a}_DEAG" name="$c déag">
            <antipattern>
                <token>a</token>
                <token regexp="yes">$c</token>
                <token regexp="yes">dhéag</token>
            </antipattern>
            <antipattern>
                <token>$d</token>
                <token></token>
                <token>déag</token>
            </antipattern>
            <antipattern>
                <token>nó</token>
                <token>$c</token>
                <token>déag</token>
            </antipattern>
            <rule>
                <pattern>
                    <token regexp="yes">$b</token>
                    <token regexp="yes">dh?éag</token>
                </pattern>
                <message>Ní úsáidtear an focal seo ach sna abairtíní <suggestion>a $c dhéag<suggestion> nó <suggestion>$d X déag</suggestion> de ghnáth</message>
                <short>In abairt</short>
                <example correction="a $c dhéag|$d X déag"><marker>$c déag</marker></example>
            </rule>
            <rule>
                <pattern>
                    <token>a</token>
                    <token regexp="yes">$b</token>
                    <marker>
                    <token>déag</token>
                    </marker>
                </pattern>
                <message>Ba chóir duit <suggestion>dhéag</suggestion> a scríobh.</message>
                <example correction="dhéag">a $c <marker>déag</marker></example>
            </rule>
        </rulegroup>
__END__
}