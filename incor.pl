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
    if(/<[^>]+>([^<]+)<\/[^>]+>:IONADAI\{([^}]+)\}/) {
        write_rule($1, $2);
    }
}

sub write_rule {
    my $tok = shift;
    my $out = shift;
    my $in = $tok;
    my $regex = ($tok =~ /\?/) ? " regexp=\"yes\"" : "";
    $in =~ s/.\?//g;
    my $uin1 = uc(unfada($in));
    print<<__END__;
        <rule id="${uin1}" name="$in">
            <pattern>
                <token$regex>$tok</token>
            </pattern>
            <message>Focal ceart ach tá <suggestion>$out</suggestion> níos coitianta.</message>
            <short>Neamhchoitianta</short>
            <example correction="$out"><marker>$in</marker></example>
        </rule>
__END__
}