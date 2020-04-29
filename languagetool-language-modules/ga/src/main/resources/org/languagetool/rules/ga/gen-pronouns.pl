#!/usr/bin/perl
# Generates contracted prepositions + pronouns
# Licence: MIT
use warnings;
use strict;
use utf8;

my @prnname = qw/mé tú é í sinn sibh iad mise tusa eisean ise sinne sibhse iadsan/;
my @prnrule = qw/mé tú|thú é í|sí sinn|muid sibh iad|siad mise tusa|thusa eisean|seisean ise|sise sinne|muidne|muide sibhse iadsan|siadsan/;

binmode(STDOUT, ":utf8");

my %forms = (
"chuig" => [ "chugam", "chugat", "chuige", "chuici", "chugainn", "chugaibh", "chucu", "chugamsa", "chugatsa", "chuigesean", "chuicise", "chugainne", "chugaibhse", "chucusan" ],
"ar" => [ "orm", "ort", "air", "uirthi", "orainn", "oraibh", "orthu", "ormsa", "ortsa", "airsean", "uirthise", "orainne", "oraibhse", "orthusan" ],
"faoi" => [ "fúm", "fút", "faoi", "fúithi", "fúinn", "fúibh", "fúthu", "fúmsa", "fútsa", "faoisean", "fúithise", "fúinne", "fúibhse", "fúthusan" ],
"as" => [ "asam", "asat", "as", "aisti", "asainn", "asaibh", "astu", "asamsa", "asatsa", "as-san", "aistise", "asainne", "ashaibhse", "astusan" ],
"do" => [ "dom", "duit", "dó", "di", "dúinn", "daoibh", "dóibh", "domsa", "duitse", "dósan", "dise", "dúinne", "daoibhse", "dóibhsean" ],
"de" => [ "díom", "díot", "de", "di", "dínn", "díbh", "díobh", "díomsa", "díotsa", "desean", "dise", "dínne", "díbhse", "díobhsan" ],
"i" => [ "ionam", "ionat", "ann", "inti", "ionainn", "ionaibh", "iontu", "ionamsa", "ionatsa", "annsan", "intise", "ionainne", "ionaibhse", "iontusan" ],
"fara" => [ "faram", "farat", "fairis", "farae", "farainn", "faraibh", "faru", "faramsa", "faratsa", "fairis-sean", "faraese", "farainne", "faraibhse", "farusan" ],
"ó" => [ "uaim", "uait", "uaidh", "uaithi", "uainn", "uaibh", "uathu", "uaimse", "uaitse", "uaidhsean", "uaithise", "uainne", "uaibhse", "uathusan" ],
"trí" => [ "tríom", "tríot", "tríd", "tríthi", "trínn", "tríbh", "tríothu", "tríomsa", "tríotsa", "trídsean", "tríthise", "trínne", "tríbhse", "tríothusan" ],
"roimh" => [ "romham", "romhat", "roimhe", "roimpi", "romhainn", "romhaibh", "rompu", "romhamsa", "romhatsa", "roimhesean", "roimpise", "romhainne", "romhaibhse", "rompusan" ],
"um" => [ "umam", "umat", "uime", "uimpi", "umainn", "umaibh", "umpu", "umamsa", "umatsa", "uimesean", "uimpise", "umainne", "umaibhse", "umpusan" ],
"ionsar" => [ "ionsorm", "ionsort", "ionsair", "ionsuirthi", "ionsorainn", "ionsoraibh", "ionsorthu", "ionsormsa", "ionsortsa", "ionsairsean", "ionsuirthise", "ionsorainne", "ionsoraibhse", "ionsorthusan" ],
"thar" => [ "tharam", "tharat", "thairis", "thairsti", "tharainn", "tharaibh", "tharstu", "tharamsa", "tharatsa", "thairis-sean", "thairstise", "tharainne", "tharaibhse", "tharstusan" ],
"chun" => [ "chugam", "chugat", "chuige", "chuici", "chugainn", "chugaibh", "chucu", "chugamsa", "chugatsa", "chuigesean", "chuicise", "chugainne", "chugaibhse", "chucusan" ],
);

sub unfada {
    my $in = shift;
    $in =~ s/á/a/g;
    $in =~ s/é/e/g;
    $in =~ s/í/i/g;
    $in =~ s/ó/o/g;
    $in =~ s/ú/u/g;
    $in =~ s/Á/A/g;
    $in =~ s/É/E/g;
    $in =~ s/Í/I/g;
    $in =~ s/Ó/O/g;
    $in =~ s/Ú/U/g;
    $in;
}

for my $prep (keys %forms) {
    my $upprep = uc($prep);
    print "        <rulegroup id=\"${upprep}_CONTRACTIONS\" name=\"$prep Contractions\">\n";
    for(my $i = 0; $i <= $#prnname; $i++) {
        my $prn = $prnname[$i];
        my $pat = $prnrule[$i];
        my $out = ${$forms{$prep}}[$i];
        my $upprn = unfada(uc($prn));
        my $upout = unfada(uc($out));
        my $pattok = ($pat =~ /\|/) ? "<token regexp=\"yes\">$pat</token>" : "<token>$pat</token>";
        my $output=<<__END__;
            <rule id="${upprep}_${upprn}_${upout}" name="$prep + $prn = $out">
                <pattern>
                    <marker>
                        <token>$prep</token>
                        $pattok
                    </marker>
                </pattern>
                <message>Ba chóir duit <suggestion>$out</suggestion> a scríobh.</message>
                <example correction='$out'><marker>$prep $prn</marker></example>
            </rule>
__END__
    print $output;
    }
    print "        </rulegroup>\n";
}
