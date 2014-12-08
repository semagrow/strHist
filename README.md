GETTING STARTED
=======

### Abstract

This is an extension to the STHoles algorithm for RDF data, based on URI prefixes.

### Building project

**Prerequisites**

* git
* mvn
* java 1.7 or above

**Instructions**

* Clone from [bitbucket](git@bitbucket.org:acharal/sthist.git)
* Switch branch to 'multiproject'
* Change directory into the project root, etc *path_to_project*/sthist/
* Run 'mvn package' to build project
* The jar can be found in *path_to_project*/sthist/evaluation/target/

### Experiments & Evaluation

The experiment procedures are divided in 3 main executables for preparing the workload, refining and evaluating it. The abstract order of execution is as follows:

1. Prepare training workload
2. Refine training torkload
3. Evaluate

`PrepareTrainingWorkload.java`: Queries a repository and intercepts the feedback, writing it as logs using java serialization by default in /var/tmp/.

`RefineTrainingWorkload.java`: Parses the logs and refines a histogram with the query feedback. The output destination is given by the user. The histogram is serialized at the end of refinement using JSON and VOID formats (VOID format can be loaded and visualized in [Eleon](git@bitbucket.org:bigopendata/eleon.git).

`Evaluate.java`: Parses the histogram and executes point queries both in histogram and repository, so as to get the estimated and actual cardinality of each point query.

In our setup the above executables are being repeatedly run for each triple store using unix scripts. We process the final results using unix scripts in order to extract statistics as the absolute average error, the percentage of non root evaluation and the only-root evaluation (the final results are being printed as *Year, Prefix, Act, Est, AbsErr%*.

Running a demo
-----------

The code for the experiment setup can be found and tuned in 'package gr.demokritos.iit.irss.semagrow.tools'.

### Setup Description

A *histogram* needs training workload in order to be refined so the first thing to be done is to prepare it for consumption. We use *SPARQL* for getting workload and we do that by quering a triple store. In case there is no triple store locally available, someone could use HTTP SPARQL querying in order to get the desired workload. The only thing to be changed is the instantiation of the Sail Repository, in *getRepository()* inside `Utils.java`. The workload are queries we artifially produced by trimming URIs (to create prefixes) we found on our triple store. Other setup adjustments can be done by editing `PrepareTrainingWorkload.java`, such as the query to be evaluated, the number of queries etc. 

After having the training workload prepared, the histogram is ready to be instatiated and trained. This is done by running  `RefineTrainingWorkload.java`, which parses the data from the previous step and creates a histogram and refines it based on that workload. At this point someone could visualize the histogram by using any of our serialization formats (json and void).

The evaluation of the histogram is being conducted as follows: We have prepared artificial point queries which are being fired both on the triple store (to get the actual cardinality) and the histogram (to get the estimated cardinality). 

### How to run
After cloning and building project using mvn, the executable can be run as follows (Dlog4j is optional). Example for preparing training workload:
`java -Dlog4j.configuration=file:/path_to_log4j.properties/ -cp /path_to_jar/ gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix.PrepareTrainingWorkload {various_options}`






