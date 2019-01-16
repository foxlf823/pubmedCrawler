#!/bin/bash
ct=0
for file in `ls $1`
do
# nohup java Main ./pubmed_snomed_$ct $1/$file 10 1 > output_$file.log 2>&1 &
    ct=`expr $ct + 1`
    nohup java Main ./pubmed_snomed_$file $1/$file 10 1 > output_$file.log 2>&1 &
done
