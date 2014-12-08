ABS_PATH=/mnt/ocfs2/IDF_data/journals/exp_triples/histogram/test/experiments/infinite_fixed_root_no_merges
Y=$1

awk '{print $3 $4}' $ABS_PATH/results_$Y.csv | sed "s/,/ /g" | awk '!/.\s?7/' > $ABS_PATH/results__$Y.csv
