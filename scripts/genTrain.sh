#!/bin/bash

i=1;

if [[ x"$1" == x ]]; then P="b";
else P="$1"; fi

while [[ 1==1 ]]; do
    F=$(printf "%05d\n" $i)
    D="train/$P$F/"
    mkdir -p $D
    java -cp ./lib/*:./lib/cp gr.demokritos.iit.irss.semagrow.TestMainQueryFeedbackGenerate 50 sorted f/ "$D"
    DNUM="train_num/$P$F/"
    mkdir -p $DNUM
    java -cp ./lib/*:./lib/cp gr.demokritos.iit.irss.semagrow.tools.RDFtoNumQueryConverter -s sorted -t "$D" -o "$DNUM"

    i=$((i+1))
done

exit 0;
