ABS_PATH=/mnt/ocfs2/IDF_data/journals/exp_triples/histogram/test

OPTS="-i /mnt/ocfs2/IDF_data/journals/exp_triples/"

CP=${ABS_PATH}/jars/findTrainNumb3rsByDistribution.jar

for Y in {1977..2004}
do

FILE=log4j.$Y.properties
LOG4J=${ABS_PATH}/log4j_properties_train/$FILE
cat ${ABS_PATH}/log4j_properties_train/log4j.properties | sed "s|{year}|$Y|" > $LOG4J

java -Dlog4j.configuration=file:$LOG4J -cp $CP gr.demokritos.iit.irss.semagrow.tools.ExtractRepoStats -y $Y $OPTS

done

