#!/usr/bin/perl
#
# Usage: fixjad.pl jarfile.jar jadfile.jad
# Fixes jar filename and size in 'jad' file
#

if (scalar @ARGV != 2)
 {usage; exit 2;}

$jarfile = $ARGV[0];
$jadfile = $ARGV[1];

if ($jarfile !~ /.jar$/ || $jadfile !~ /.jad$/)
 {usage; exit 2;}

open JD, $jadfile || die "Cannot open $jadfile for reading";
@jadlines=<JD>;
close JD;

open JD, ">$jadfile" || die "Cannot open $jadfile for writing";

$jarbase = `basename $jarfile`;
chomp $jarbase;

for (@jadlines)
{
    if      (/^MIDlet-Jar-Size:/) {print JD "MIDlet-Jar-Size: ".(-s $jarfile)."\n";}
    elsif (/^MIDlet-Jar-URL:/) {print JD "MIDlet-Jar-URL: $jarbase\n";}
    else    {print JD;}
}

close JD;

sub usage
{
    print "Usage: $0 <jarfile> <jadfile>";
}
