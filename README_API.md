### CODE OVERVIEW

The STRHist codebase is organized around the following interface packages:

* The API specifies database *histograms*, including *self-tuning histograms* that can be updated through query feedback.

* The RDF, STHOLES, STHOLES PREFIX deal with the implementations of *self-tuning histograms*. The different types of histograms denote the different types and dimentionality of ranges of the <Rectangle> respectively.

* The QFR specifies the management of handling (parsing and writing) metadata and query results.


### API

***Prefix Path***

* api/src/main/java/gr.democritos.iit.irss.semagrow/api/

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
   
→ QueryResultImpl: implements <QueryResult>. Based on the resultset it contains the <RDFRectangle> with some statistics.
* QueryLogRecordImpl: implements <QueryLogRecord> in order to keep metadata.
* SerialQueryLogRecord: implements <QueryLogRecord> it is used for serialization of a <QueryLogRecord> (for read and write from/to the supplied input/output stream).
* RDFQueryLogHandler: implements <QueryLogHandler>. Handles/writes a <QueryLogRecord> object in RDF format – as triples to the supplied output stream of log file.
* SerialQueryLogHandler: implements <QueryLogHandler>. Writes a <SerialQueryLogRecord> to the supplied output stream of the log file.
* RDFQueryLogParser: implements <QueryLogParser>. Reads an RDF model from the supplied input stream of log file and parses the information to a <QueryLogRecord> object.
* SerialQueryLogHandler: implements <QueryLogParser>. Reads a <SerialQueryLogRecord> from the supplied input stream of the log file.
* RDFQueryLogFactory: implements <QueryLogFactory>. Returns a <RDFQueryLogHandler> for writing, based on RDFWriter.
* SerialQueryLogFactory: implements <QueryLogFactory>. Returns a <SerialQueryLogHandler> that will write to the provided output stream.
* FileManager: implements <ResultMaterializationManager> for handling results from files. 
	- getResults(): get the results from a specified input stream. The parsing uses a queue to load a small part at a time (consumer-producer).
 	- saveResults(): returns a <StoreHandler> with a supplied output stream of the result file.
  
→ StoreHandler: implements <MaterializationHandle>. It handles/commits the results through endQueryResults() function.


