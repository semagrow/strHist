### CODE OVERVIEW

The STRHist codebase is organized around the following interface packages:

* The API specifies database *histograms*, including *self-tuning histograms* that can be updated through query feedback.

* The RDF, STHOLES, STHOLES PREFIX deal with the implementations of *self-tuning histograms*. The different types of histograms denote the different types and dimensionality of ranges of the <Rectangle> respectively.

* The QFR specifies the management of handling (parsing and writing) metadata and query results.

* The TOOLS specifies the main functionality of loading the histogram and refine it with the query results sets that another system produced.


### API

***Prefix Path***

* api/src/main/java/gr.demokritos.iit.irss.semagrow/api/

***Interfaces***

* Histogram: basic interface of an histogram consisted of Rectangles
* Rectangle: multidimensional bounding box
* STHistogram: a Self Tuning Histogram – extends <Histogram>
* QueryRecord: a query feedback record that contains the query and its resultset (<QueryResult>)
* QueryResult: the resultset as a list of Rectangles


### RDF, STHOLES, STHOLES PREFIX

***Prefix Paths***

* rdf/src/main/java/gr.demokritos.iit.irss.semagrow.rdf
* stholes/src/main/java/gr.demoktiros.iit.semagrow.stholesOrig
* stholes-prefix/src/main/java/gr.demokritos.iit.irss.semagrow.stholes

***Implementations***

* RDFSTHolesHistogram: extends <STHolesHistogram>. Contains <RDFRectangle> - 3D ranges of subject, predicate, object.
* STHolesOrigHistogram: implements <STHistogram>. Represents the original histogram with enumeration ranges.
* STHistogramBase: implements <STHistogram>. The basic functionality is the refinement/update of the histogram by using a query feedback records (<QueryRecord>).
* STHolesHistogram: extends <STHistogramBase>. Implements a <STHistogram> with n-dimensional ranges.


### QFR

***Prefix Paths***

Path for Interfaces

* qfr/src/main/java/gr.demokritos.iit.irss.semagrow/api/

Paths for Implementation

* qfr/src/main/java/gr.demokritos.iit.irss.semagrow/impl/
* qfr/src/main/java/gr.demokritos.iit.irss.semagrow/file/

***Interfaces***

* QueryLogRecord: interface for metadata.
* QueryLogHandler: interface to handle/write the log file.
* QueryLogParser: interface to parse/read the log file.
* QueryLogFactory: returns a <QueryLogHandler> instance that will write to the supplied output stream of log file.
* ResultMaterializationManager: interface to store and get results.
* MaterializationHandle: interface for handling the results.

***Implementations***

* QueryRecordAdapter: implements <QueryRecord>. Based on the metadata file, it takes the query results – patterns from the suitable result-File (with the use of <FileManager>). Take in mind that only single-pattern queries are supported. Then, it computes the RDFRectangle by using this pattern. The computation of the RDFRectangle denotes the computation of its 3 ranges – one for subject that can only contain prefixes (URI), the other for predicate that can be either prefixes or literals and the last one for objects which can take many different forms.
   
* QueryRecordAdapter.QueryResultImpl: implements <QueryResult>. Based on the resultset it contains the <RDFRectangle> with some statistics.
* QueryLogRecordImpl: implements <QueryLogRecord> in order to keep metadata.
* SerialQueryLogRecord: implements <QueryLogRecord> it is used for serialization of a <QueryLogRecord> (for read and write from/to the supplied input/output stream).
* RDFQueryLogHandler: implements <QueryLogHandler>. Handles/writes a <QueryLogRecord> object in RDF format – as triples to the supplied output stream of log file.
* SerialQueryLogHandler: implements <QueryLogHandler>. Writes a <SerialQueryLogRecord> to the supplied output stream of the log file.
* RDFQueryLogParser: implements <QueryLogParser>. Reads an RDF model from the supplied input stream of log file and parses the information to a <QueryLogRecord> object.
* SerialQueryLogHandler: implements <QueryLogParser>. Reads a <SerialQueryLogRecord> from the supplied input stream of the log file.
* RDFQueryLogFactory: implements <QueryLogFactory>. Returns a <RDFQueryLogHandler> for writing, based on RDFWriter.
* SerialQueryLogFactory: implements <QueryLogFactory>. Returns a <SerialQueryLogHandler> that will write to the provided output stream.
* FileManager: implements <ResultMaterializationManager> for handling results from files.
___→  getResults(): get the results from a specified input stream. The parsing uses a queue to load a small part at a time (consumer-producer).
<br />
___→  saveResults(): returns a <StoreHandler> with a supplied output stream of the result file.
  
* FileManager.StoreHandler: implements <MaterializationHandle>. It handles/commits the results through endQueryResults() function.

### TOOLS

Paths for Interfaces

* tools/src/main/java/gr.demokritos.iit.irss.semagrow/config/
* tools/src/main/java/gr.demokritos.iit.irss.semagrow/log/

Paths for Implementation

* tools/src/main/java/gr.demokritos.iit.irss.semagrow/config/
* tools/src/main/java/gr.demokritos.iit.irss.semagrow/histogram/
* tools/src/main/java/gr.demokritos.iit.irss.semagrow/log/
* tools/src/main/java/gr.demokritos.iit.irss.semagrow/qfr/

***Interfaces***

* LogConfig: interface for configuration information about the histogram and query log files.
* LogWriter: interface to write information and errors of the procedure to an extra log file.


***Implementations***

* LogConfigImpl: implements <LogConfig>. The required configuration information that we should provide, is the directories of histogram and query logs, the file prefix of the query logs as well as a flag that indicates whether to delete query log files after refinement.
* HistogramUtils: includes utility methods for loading and keep backup of a histogram, adapt the record query logs to specific structures etc.
* LoadHistogram: refines the histogram with the results that are included into a specific query log file.
* LogWriterImpl: implements <LogWriter>. It contains a public static factory method - getInstance() - in order to record the procedure of loading a histogram, parsing the query log files, refining a histogram and throwing potential exceptions.
* QfrLastParser: reads from a file the timestamp of the last query log file where the system parsed.
* QfrLastWriter: writes to a file the timestamp of the query log file where the system just parsed and refined the histogram with its resultsets.
* QueryLogManager: decides and returns the query log files that will be used for refinement.
* QueryLogReader: parses a query log file with the use of <RDFQueryLogParser> and converts the query log files' collection to a suitable form for refinement. Also, it is responsible for shutting down the parser and deleting the result files, if this choice is specified.
* QueryLogRemover: It contains a public static factory method and removes a specified - query log or result - file.

***Main Procedure***

The necessary arguments for the execution are:
* -h <path_to_histogram>: the path where the histogram will be stored.
* -l <path_to_logs>: the path where the log and result files are stored.
* -p <filePrefix>: the prefix of the log files (ex. qfr)
* -d: indicates whether to delete log and result files after refinement.

The system can be executed at any time manually or with the use of a time-based job scheduler. The main challenge here is the independence of the system with the other systems that handles the query log files, such as Semagrow.
Therefore, the system takes into account only these query log files where are not used from Semagrow and are not utilized from a previous refinement. Specifically, Semagrow uses a rotation procedure where at every *counter* queries it creates and handles a new query log file.
The name of this new query log file denotes the ordering. Taking into consideration this rotation procedure, we ignore the last file based on an alphanumerical file sorting. In addition, the system
keeps the timestamp of the query log file where lastly was parsed for refinement, in order to avoid the next parsing. Moreover, a backup of histogram's (de-)serialization is kept and updated at every refinement to avoid the miss of histograms' files when an unexpected error occurs during writing.

***Exceptions***

Path for Exceptions

* tools/src/main/java/gr.demokritos.iit.irss.semagrow/exception/

Extensions of <Exception>

* HistogramException: used for exceptions that are thrown during the handle of a histogram - backup and its (de-)serialization. The exit code of this kind of exception is *6*.
* LogException: this exception is thrown when handling the extra log file. The exit code is *7*.
* IntegrationException: this exception is thrown at any other error during the procedure. Specifically,
___→ Exit code *2* denotes casting errors.
<br >
___→ Exit code *3* denotes an ArrayIndexOutOfBoundsException.
<br >
___→ Exit code *4* denotes a problem during the parsing of a query log file.
<br >
___→ Exit code *5* denotes a problem in handling the file that contains the timestamp of the last parsed query log file.

