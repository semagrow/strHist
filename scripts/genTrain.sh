#!/bin/bash

i=1;

while [[ 1==1 ]]; do
    F=$(printf "%05d\n" $i)
    echo "java -cp ./lib/*:./lib/cp gr.demokritos.iit.irss.semagrow.TestMainQueryFeedbackGenerate 50 sorted f/ train/b$F/"
    i=$((i+1))
done

exit 0;
