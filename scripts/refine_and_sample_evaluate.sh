ABS_PATH=/mnt/ocfs2/IDF_data/journals/exp_triples/histogram/test

OPTS="-o /mnt/ocfs2/IDF_data/journals/exp_triples/histogram/test/experiments/experimentNo5/ -i /mnt/ocfs2/IDF_data/journals/exp_triples/"

CP=${ABS_PATH}/jars/refine_and_sample_evaluate.jar

for Y in {1990..2001}
do

FILE=log4j.$Y.properties
LOG4J=${ABS_PATH}/log4j_properties_samples/$FILE
cat ${ABS_PATH}/log4j_properties_samples/log4j.properties | sed "s|{year}|$Y|" > $LOG4J

java -Dlog4j.configuration=file:$LOG4J -cp $CP gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix.RefineAndEvaluate -y $Y $OPTS

done

