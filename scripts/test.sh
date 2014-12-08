#!/bin/sh

ABS_PATH=/mnt/ocfs2/IDF_data/journals/exp_triples/histogram/test


v1=$(grep 'triples: *' $ABS_PATH/logs/exper_infinite_fixed_root_no_merges_evaluation/$1.log  | wc -l)

v2=$(grep 'triples: 7' $ABS_PATH/logs/exper_infinite_fixed_root_no_merges_evaluation/$1.log  | wc -l)

printf "$v1 $v2"

#for Y in {1977..1996}; do sh scripts/test.sh $Y | awk '{print 100-$2*100/$1}'; done;
