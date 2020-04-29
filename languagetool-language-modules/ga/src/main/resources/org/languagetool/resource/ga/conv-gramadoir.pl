#!/usr/bin/perl
# Converts rules from an Gramadóir
# Licence: MIT
use warnings;
use strict;
use utf8;

binmode(STDIN, ":utf8");
binmode(STDOUT, ":utf8");

my %TOKEN = (
    '<N>UNLENITED</N>' => '<token postag=".*Noun.*" postag_regexp="yes"><exception postag="*:Len" postag_regexp="yes"/></token>',
    '<A>UNLENITED</A>' => '<token postag="Adj:.*" postag_regexp="yes"><exception postag="*:Len" postag_regexp="yes"/></token>',
    '<N pl="n" gnt="n" gnd="f">ECLIPSED</N>' => '<token postag="(?:C[UMC]:)?Noun:Fem:Com:Sg:Ecl" postag_regexp="yes"></token>',
    '<S>COMPOUND</S>' => '<token postag="&lt;/Prep:Cmpd&gt;"></token>',
    '<S>NONCOMPOUND</S>' => '<token postag=".*Prep.*" postag_regexp="yes"></token>',
);

my %PARTTOKEN = (
    '<V cop="y">' => '.*Cop:.*',
    '<N pl="n" gnt="n" gnd="f">' => '(?:C[UMC]:)?Noun:Fem:Com:Sg.*',
    '<N pl="n" gnt="n" gnd="m">' => '(?:C[UMC]:)?Noun:Masc:Com:Sg.*',
    '<N pl="n" gnt="y" gnd="f">' => '(?:C[UMC]:)?Noun:Fem:Gen:Sg.*',
    '<N pl="n" gnt="y" gnd="m">' => '(?:C[UMC]:)?Noun:Masc:Gen:Sg.*',
    '<N pl="y" gnt="n" gnd="f">' => '(?:C[UMC]:)?Noun:Fem:Com:Pl.*',
    '<N pl="y" gnt="n" gnd="m">' => '(?:C[UMC]:)?Noun:Masc:Com:Pl.*',
    '<N pl="y" gnt="y" gnd="f">' => '(?:C[UMC]:)?Noun:Fem:Gen:Pl.*',
    '<N pl="y" gnt="y" gnd="m">' => '(?:C[UMC]:)?Noun:Masc:Gen:Pl.*',
    '<A pl="." gnt="n">' => '.*Adj.*Com.*',
    #'<A pl="n" gnt="n" h="y">' => '',
    '<A pl="n" gnt="n">' => '.*Adj.*Com.*Sg.*|.*Adj:Base.*',
    '<A pl="n" gnt="y" gnd="f">' => '.*Adj:Gen:Fem:Sg.*',
    '<A pl="n" gnt="y" gnd="m">' => '.*Adj:Gen:Masc:Sg.*',
    '<A pl="y" gnt="n">' => '.*Adj.*Com.*Pl.*',

);

my %POS = (
    'A' => '.*Adj:.*',
    'AG' => '.*Adj:.*Gen.*',
    'AP' => '.*Adj:.*Pl.*',
    'AS' => '.*Adj:.*Sg.*',
    'N' => '.*Noun.*',
    'NG' => '.*Noun.*:Gen.*',
    'NFCS' => '.*Noun:Fem:Com:Sg',
    'NMCS' => '.*Noun:Masc:Com:Sg',
    'NCS' => '.*Noun:(?:Fem|Masc):Com:Sg',
    'NFGS' => '.*Noun:Fem:Gen:Sg',
    'NMGS' => '.*Noun:Masc:Gen:Sg',
    'NP' => '.*Noun:.*:Pl',
    '[NY]' => '.*Noun.*',
    'S' => '.*Prep.*|&lt;/Prep:Cmpd&gt;',
    'T' => '.*:Art.*',
    'Q' => '.*:(?:Q|NegQ).*',
    'C' => '.*Conj:.*',
    'Y' => '.*Prop:Noun.*',
);

my @mentities = qw/
lenitedfuture
abairpast
abairprfu
faighfc
abspastverb
absnonpastverb
dependent
justta
justata
nonrformconj
nonrformprep
rformconj
rformprep
ahnumber
cheadcompound
noncompound
compound
synthpast
allgenitivepreps
genitiveprep
irregularpast
pastnorformlen
pastnorform
pastafterni
faigheclipsed
faightoeclipse
positiveint
twotonineteen
vowelnumeral
vowelordinal
vowelnumadj
nibs
unleniteds
initialvowelorf
initialvowel
initialconsonant
nonvowelnonf
uneclipseddt
uneclipsedcons
uneclipsed
eclipsedvowel
eclipseddt
eclipsedbcfgp
eclipsed
maybeeclipsingnumber
eclipsingnumber
eclipsingposs
unboundadj
unpplike
fakepp
unlenitable
unlenitedbcgmp
unlenitedbcfgmp
unmutatedvnish
unmutatedbcfgp
unmutated
unlenitedf
unlenitedcdfgst
unlenited
lenitedf
ordinaladj
prefixedt
eire
lenitedngmperson
lenitedngfperson
ngmperson
ngfperson
regularposs
fusedposs
fusedprep
dativeprep
initialmordapost
initialbigdapost
initialdapostf
initialdapost
initialbapost
initialmbapost
lenitedcapcg
lenitedbcfgmps
lenitedbmp
mutateddst
leniteddfst
leniteddst
lenited
slenderfinalconsonant
broadfinalconsonant
finalvowel
finala
slenderfinaldlnst
finaldlnst
dayoftheweek
nobeeapost
femvn
subjectpronoun
notvnishunlen
notvnishvn
vnish
femabstractrestricted
femabstract
quantityword
doword
arword
initialc
initialdst
initialf
initialh
initiall
initialm
initialn
initials
initialts
notna
broadfirstpres
slenderfirstpres
broadsecondpres
slendersecondpres
broadfirstfuture
slenderfirstfuture
broadsecondfuture
slendersecondfuture
broadfirstcond
slenderfirstcond
broadsecondcond
slendersecondcond
broadfirstimp
slenderfirstimp
broadsecondimp
slendersecondimp
/;

my %entities = map { $_ => 1 } @mentities;

my %msg = (
    "ABSOLUTE" => "Níl gá leis an fhoirm spleách",
    "AIDIOLRA" => "Ba chóir duit aidiacht iolra a úsáid anseo",
    "ANAITHNID" => "Focal anaithnid",
    "BACHOIR" => "Ba chóir duit ‘\\1’ a úsáid anseo",
    "BADART" => "Níl gá leis an alt cinnte anseo",
    "BREISCHEIM" => "Ba chóir duit an bhreischéim a úsáid anseo",
    "CAIGHDEAN" => "Foirm neamhchaighdeánach de ‘\\1’",
    "CAIGHMOIRF" => "Bunaithe ar fhoirm neamhchaighdeánach de ‘\\1’",
    "CLAOCHLU" => "Urú nó séimhiú ar iarraidh",
    "CONDITIONAL" => "Ba chóir duit an modh coinníollach a úsáid anseo",
    "CUPLA" => "Cor cainte aisteach",
    "DROCHMHOIRF" => "Bunaithe go mícheart ar an bhfréamh ‘\\1’",
    "GENDER" => "Inscne mhícheart",
    "GENITIVE" => "Tá gá leis an leagan ginideach anseo",
    "INPHRASE" => "Ní úsáidtear an focal seo ach san abairtín ‘\\1’ de ghnáth",
    "IOLRA" => "Tá gá leis an leagan iolra anseo",
    "IONADAI" => "Focal ceart ach tá ‘\\1’ níos coitianta",
    "MICHEART" => "An raibh ‘\\1’ ar intinn agat?",
    "NEAMHCHOIT" => "Focal ceart ach an-neamhchoitianta - an é atá uait anseo?",
    "NEEDART" => "Ba chóir duit an t-alt cinnte a úsáid",
    "NIAITCH" => "Réamhlitir ‘h’ gan ghá",
    "NIBEE" => "Réamhlitir ‘b'’ gan ghá",
    "NICLAOCHLU" => "Urú nó séimhiú gan ghá",
    "NIDARASEIMHIU" => "Ní gá leis an dara séimhiú",
    "NIDEE" => "Réamhlitir ‘d'’ gan ghá",
    "NIGA" => "Níl gá leis an fhocal ‘\\1’",
    "NISEIMHIU" => "Séimhiú gan ghá",
    "NITEE" => "Réamhlitir ‘t’ gan ghá",
    "NIURU" => "Urú gan ghá: ",
    "NODATIVE" => "Ní úsáidtear an tabharthach ach in abairtí speisialta: ",
    "NOGENITIVE" => "Níl gá leis an leagan ginideach anseo: ",
    "NOSUBJ" => "Ní dócha go raibh intinn agat an modh foshuiteach a úsáid anseo: ",
    "NUMBER" => "Uimhir mhícheart: ",
    "OK" => "",
    "ONEART" => "Níl gá leis an gcéad alt cinnte anseo: ",
    "PREFIXD" => "Réamhlitir ‘d'’ ar iarraidh: ",
    "PREFIXH" => "Réamhlitir ‘h’ ar iarraidh: ",
    "PREFIXT" => "Réamhlitir ‘t’ ar iarraidh: ",
    "PRESENT" => "Ba chóir duit an aimsir láithreach a úsáid anseo: ",
    "RELATIVE" => "Tá gá leis an fhoirm spleách anseo: ",
    "SEIMHIU" => 'Séimhiú ar iarraidh: ',
    "SYNTHETIC" => "Is an fhoirm tháite, leis an iarmhír ‘\\1’, a úsáidtear go minic",
    "UATHA" => "Tá gá leis an leagan uatha anseo: ",
    "URU" => "Urú ar iarraidh: ",
    "WEAKSEIMHIU" => "Leanann séimhiú an réamhfhocal ‘\1’ go minic, ach ní léir é sa chás seo",

);

sub mktoken {
    my $in = shift;
    if(exists $TOKEN{$in}) {
        return $TOKEN{$in};
    } elsif ($in =~ /(<([^ ]*)[^>]*>)([^<]*)<[^>]*>/) {
        my $rawfulltok = $1;
        my $postag = $2;
        my $inner = $3;
        my $tokout = '<token';
        if(exists $PARTTOKEN{$rawfulltok}) {
            $tokout .= " postag=\"$PARTTOKEN{$rawfulltok}\" postag_regexp=\"yes\"";
        } elsif(exists $POS{$postag}) {
            $tokout .= " postag=\"$POS{$postag}\" postag_regexp=\"yes\"";
        } elsif($postag eq 'X') {
            # Do nothing
        } else {
            print STDERR "WARNING: unknown POS tag $postag\n";
        }
        if($inner eq 'ANYTHING') {
            $tokout .= '></token>';
            return $tokout;
        } elsif($inner =~ /[A-Z][A-Z]+/) {
            if(exists $entities{lc($inner)}) {
                $tokout .= " regexp=\"yes\">&" . lc($inner) .";</token>";
                return $tokout;
            } else {
                print STDERR "CONVERSION_FAILURE: $inner\n";
                return "";
            }
        } else {
            if($inner =~ /[\?\|\(]/) {
                $tokout .= " regexp=\"yes\">$inner</token>";
                return $tokout;
            } else {
                $tokout .= ">$inner</token>";
                return $tokout;
            }
        }
    } else {
        print STDERR "A bad thing happened\n";
    }
}

sub macro_to_entity {
    my $in = shift;
    if($in =~ /^s\/([^\/]*)[\/](.*)\/g;$/) {
        my $name = $1;
        my $regex = $2;
        $name = lc($name);
        $regex =~ s/\[\^<\]\+/.+/g;
        $regex =~ s/\[\^<\]\*/.*/g;
        return "        <!ENTITY $name \"$regex\">\n";
    } else {
        return "";
    }
}

sub num_bachoir {
    my $in = shift;
    print "<!-- $in -->\n";
    
    my $num;
    my $word;
    my $repl;
    if($in =~ /^<R>NIBS<\/R> <A [^>]+>([^<]+)<\/[^>]*>:BACHOIR\{([^\}]+)\}$/) {
        write_nibs($1, $2);
    } elsif($in =~ /^<[A-Z]+[^>]*>([^<]+)<\/[^>]*> <[A-Z]+[^>]+>([^<]+)<\/[^>]*>:BACHOIR\{([^\}]+)\}$/) {
    print "<!-- 1 -->\n";
        return write_bachoir_simple_second($1, $2, $3);
	} elsif($in =~ /^([^ <]+) <[A-Z]+[^>]+>([^<]+)<\/[^>]*>:BACHOIR\{([^\}]+)\}/) {
    print "<!-- 2 -->\n";
        return write_bachoir_simple_second($1, $2, $3);
	} elsif($in =~ /^<[A-Z]+[^>]*>([^<]+)<\/[^>]*> ([^ :<]+):BACHOIR\{([^\}]+)\}/) {
    print "<!-- 3 -->\n";
        return write_bachoir_simple_second($1, $2, $3);
    } else {
        return "";
    }
}

sub write_nibs {
    my $word = shift;
    my $repl = shift;

	my $titleword = uc($word);
    $titleword =~ s/.\?//g;
	my $title = 'NIBS_' . $titleword;
    my $isregex = ($word =~ /[\?\|]/) ? ' regexp="yes"' : '';

	my $egword = $word;
    $egword =~ s/.\?//g;

my $out=<<__END__;
        <rulegroup id="$title" name="ní ba $egword">
            <rule>
                <pattern>
                    <token>ní</token>
                    <token>ba</token>
                    <marker>
                        <token$isregex>$word</token>
                    </marker>
                </pattern>
                <message>Ba chóir duit <suggestion>$repl</suggestion> a scríobh.</message>
                <example correction='$repl'>ní ba <marker>$egword</marker></example>
            </rule>
            <rule>
                <pattern>
                    <token>ní</token>
                    <token>b</token>
                    <token regexp="yes" spacebefore="no">&apost;</token>
                    <marker>
                        <token$isregex>$word</token>
                    </marker>
                </pattern>
                <message>Ba chóir duit <suggestion>$repl</suggestion> a scríobh.</message>
                <example correction='$repl'>ní b'<marker>$egword</marker></example>
            </rule>
            <rule>
                <pattern>
                    <token>ní</token>
                    <marker>
                        <token$isregex>$word</token>
                    </marker>
                </pattern>
                <message>Ba chóir duit <suggestion>$repl</suggestion> a scríobh.</message>
                <example correction='$repl'>ní <marker>$egword</marker></example>
            </rule>
        </rulegroup>
__END__

}

sub write_bachoir_simple_second {    
    my $num = shift;
    my $word = shift;
    my $repl = shift;

	my $titlenum = uc($num);
	$titlenum =~ s/.\?//g;
	my $titleword = uc($word);
	my $title = $titlenum . '_' . $titleword;

	my $egnum = $num;
    $egnum =~ s/.\?//g;
	my $egword = $word;
    $egword =~ s/.\?//g;
    my $isregex = ($word =~ /[\?\|]/) ? ' regexp="yes"' : '';

my $out=<<__END__;
        <rule id="$title" name="$egnum $word">
            <pattern>
                <token regexp="yes">$num</token>
                <marker>
                    <token$isregex>$word</token>
                </marker>
            </pattern>
            <message>Ba chóir duit <suggestion>$repl</suggestion> a scríobh.</message>
            <example correction='$repl'>$egnum <marker>$egword</marker></example>
        </rule>
__END__

    return $out;
}

while(<>) {
	chomp;
	s/\[Aa\]/a/g;
	s/\[Áá\]/á/g;
	s/\[Bb\]/b/g;
	s/\[Cc\]/c/g;
	s/\[Dd\]/d/g;
	s/\[Ee\]/e/g;
	s/\[Ff\]/f/g;
	s/\[Gg\]/g/g;
	s/\[Hh\]/h/g;
	s/\[Ii\]/i/g;
	s/\[Ll\]/l/g;
	s/\[Mm\]/m/g;
	s/\[Nn\]/n/g;
	s/\[Oo\]/o/g;
	s/\[Óó\]/ó/g;
	s/\[Pp\]/p/g;
	s/\[Rr\]/r/g;
	s/\[Ss\]/s/g;
	s/\[Tt\]/t/g;
	s/\[Uu\]/u/g;
	s/\[Úú\]/ú/g;

    next if(/^#/);
    next if($_ !~ /BACHOIR/);
    next if(/^[^<]/);
    
    print num_bachoir($_);
}

