---
title: mp4 Conversion for Mac OS X and Linux
tags: ffmpeg apple linux debian
---


One annoying thing about iPhone is that it does not play any video
format other than mp4. There are a bunch of GUI tools out there but the
problem is during conversion my machine crawls, as i was looking for
alternatives i came across ffmpeg, which is a cross-platform solution to
record, convert and stream audio and video. This is a quick and dirty
bash script to convert any video you pass it to and write the output to
SAVE_LOCATION.


    SAVE_LOCATION=~/Desktop/

    if [ -z "$1" ]
    then
            echo "requires file name..."
	    exit
    fi

    filename=${1##*/}
    out_file=$SAVE_LOCATION${filename%%.*}".mp4"
    out_file=${out_file// /-}

    echo "Converting: "$1
    echo "Saving:     "$out_file

    ffmpeg -i "$1" -f mp4 \
    -acodec libfaac -ar 44100 -ab 128 \
    -vcodec mpeg4 -maxrate 2000 -b 1500 \
    -qmin 3 -qmax 5 -bufsize 4096 -g 300 \
    -s 320x240 -r 30000/1001 $out_file
