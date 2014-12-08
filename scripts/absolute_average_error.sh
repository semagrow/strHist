ABS_PATH=/mnt/ocfs2/IDF_data/journals/exp_triples/histogram/test/experiments/infinite_fixed_root_with_merges_600
Y=$1

cat $ABS_PATH/results_$Y.csv | sed "s/,/ /g" | awk '{print $3-$4}' | sed "s/-//" |  awk '{s+=$1} END {printf s " "}' 

cat $ABS_PATH/results_$Y.csv | sed '/^\s*$/d' | wc -l | awk '{printf $1-1}'

#use it like below
#for Y in {1990..2001}; do printf "$Y, "; sh absolute_average_error.sh $Y | awk '{print $1/$2}'; done

