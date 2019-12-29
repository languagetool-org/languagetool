#!/usr/bin/perl
use warnings;
use strict;
use utf8;

binmode(STDOUT,":utf8");

my @things = qw/
AON;h?aon;haon;aon
TRI;d?th?rí;trí;trí
CEATHAIR;g?ch?eathair;ceathair;ceithre
CUIG;g?ch?úig;cúig;cúig
SE;sh?é;sé;sé
SEACHT;sh?eacht;seacht;seacht
OCHT;h?ocht;hocht;ocht
NAOI;naoi;naoi;naoi
/;

for my $th (@things) {
    my ($a, $b, $c, $d) = split/;/, $th;

print<<__END__;
        <rulegroup id="${a}_DEAG" name="$c déag">
            <antipattern>
                <token>a</token>
                <token>$c</token>
                <token>déag</token>
            </antipattern>
            <antipattern>
                <token>$d</token>
                <token></token>
                <token>déag</token>
            </antipattern>
            <antipattern>
                <token>nó</token>
                <token>$d</token>
                <token>déag</token>
            </antipattern>
            <rule>
                <pattern>
                    <token regexp="yes">$b</token>
                    <token regexp="yes">dh?éag</token>
                </pattern>
                <message>Ní úsáidtear an focal seo ach sna abairtíní <suggestion>a $c déag</suggestion> agus <suggestion>$d X déag</suggestion> de ghnáth</message>
                <short>In abairt</short>
                <example correction="a $c déag|$d X déag"><marker>$c déag</marker></example>
            </rule>
            <rule>
                <pattern>
                    <token>a</token>
                    <token regexp="yes">$c</token>
                    <marker>
                    <token>dhéag</token>
                    </marker>
                </pattern>
                <message>Ba chóir duit <suggestion>déag</suggestion> a scríobh.</message>
                <example correction="déag">a $c <marker>dhéag</marker></example>
            </rule>
        </rulegroup>
__END__
}