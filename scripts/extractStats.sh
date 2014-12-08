ABS_PATH=/mnt/ocfs2/IDF_data/journals/exp_triples/histogram/test

OPTS="-i /mnt/ocfs2/IDF_data/journals/exp_triples/"

CP=${ABS_PATH}/jars/extractStat.jar

for Y in {2009..2009}
do

FILE=log4j.$Y.properties
LOG4J=${ABS_PATH}/log4j_properties/$FILE
cat ${ABS_PATH}/log4j_properties/log4j.properties | sed "s|{year}|$Y|" > $LOG4J

java -Dlog4j.configuration=file:$LOG4J -cp $CP gr.demokritos.iit.irss.semagrow.tools.ExtractRepoStats -y $Y $OPTS

done

