#!/bin/sh
#
# This file runs the corresponded demo.
DEMO=midpsmtp

RUN="`dirname $0`/../../../bin/emulator -Xdescriptor:`dirname $0`/${DEMO}.jad"
echo $RUN
$RUN
