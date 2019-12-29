#!/usr/bin/perl
use warnings;
use strict;
use utf8;

binmode(STDIN,":utf8");
binmode(STDOUT,":utf8");

while (<>) {
    chomp;
    my ($pat, $pat2) = split/\t/;
    my $first = $pat;
    my $patre = '';
    if ($first =~ /\|/) {
        my @tmp = split/\|/, $first;
        $first = $tmp[0];
        $patre = ' regexp="yes"';
    }
    my $first2 = $pat2;
    if ($first2 =~ /\|/) {
        my @tmp = split/\|/, $first2;
        $first2 = $tmp[0];
    }
    my $up = uc($first);
    my $cor = $first;
    my $cor2 = $first;
    my $repl = '';
    my $repl2 = '';
    if($cor =~ /^t/) {
        $cor =~ s/^t//;
        $repl = ' regexp_match="^t(.*)" regexp_replace="$1"';
    }
    if($cor =~ /^([A-Z])h(.*)/) {
        $cor = "$1$2";
        $repl = ' regexp_match="^([A-Z])h(.*)" regexp_replace="$1$2"';
    }
    if($cor2 =~ /^t/) {
        $cor2 =~ s/^t//;
        $repl2 = ' regexp_match="^t(.*)" regexp_replace="$1"';
    }

print<<__END__;
        <rulegroup id="AN_$up" name="an $first">
            <rule>
                <pattern>
                    <token>an</token>
                    <token$patre>$pat</token>
                </pattern>
                <message>Níl gá leis an alt cinnte anseo: <suggestion><match no="2"$repl/></suggestion></message>
                <short>Níl gá leis an alt cinnte</short>
                <example correction='$cor'><marker>an $first</marker></example>
            </rule>
            <rule>
                <pattern>
                    <token regexp="yes">&fusedprep;</token>
                    <token$patre>$pat2</token>
                </pattern>
                <message>Níl gá leis an alt cinnte anseo: <suggestion><match no="1" regexp_match="(.*)n\$" regexp_replace="\$1"/> <match no="2"$repl2/></suggestion></message>
                <short>Níl gá leis an alt cinnte</short>
                <example correction='faoi $first2'><marker>faoin $first2</marker></example>
            </rule>
        </rulegroup>
__END__
}