#!/usr/bin/perl
use warnings;
use strict;
use utf8;

binmode(STDIN, ":utf8");
binmode(STDOUT, ":utf8");

#!/usr/bin/perl

sub unfada {
    my $in = shift;
    $in =~ s/á/a/;
    $in =~ s/Á/A/;
    $in =~ s/é/e/;
    $in =~ s/É/E/;
    $in =~ s/í/i/;
    $in =~ s/Í/I/;
    $in =~ s/Ó/O/;
    $in =~ s/ó/o/;
    $in =~ s/ú/u/;
    $in =~ s/Ú/U/;
    $in;
}

while(<>) {
    chomp;
    s/\r//g;
    if(/^(?:<[^>]+>)?([^< ]+)(?:<\/[^>]+>)? (?:<[^>]+>)?([A-Z][A-Z]+)(?:<\/[^>]+>)?:BACHOIR\{([^}]+)\}/) {
        print "<!-- $_ -->\n";
        write_rule($1, $2, $3);
    }
}

sub write_rule {
    my $tok = shift;
    my $pat = shift;
    my $out = shift;
    my $in = $tok;
    my $regex = ($tok =~ /\?/) ? " regexp=\"yes\"" : "";
    $in =~ s/.\?//g;
    my $ent = $pat;
    $ent =~ tr/A-Z/a-z/;
    my $uin1 = uc(unfada($in));
    print<<__END__;
        <rule id="${uin1}_${pat}" name="$in ?">
            <pattern>
                <marker>
                    <token$regex>$tok</token>
                </marker>
                <token regexp="yes">&$ent;</token>
            </pattern>
            <message>Ba chóir duit <suggestion>$out</suggestion> a scríobh.</message>
            <example correction="$out"><marker>$in</marker> ?</example>
        </rule>
__END__
}