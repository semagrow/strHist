
### CODE OVERVIEW

The STRHist codebase is organized around the following interface packages:

* The HISTOGRAMS API specifies database *histograms*, including
  *self-tuning histograms* that can be updated through query feedback

* The REFINE FEEDBACK API specifies the query feedback used by self-tuning
  histograms

* The HANDLE OF METADATA API specifies how to handle metadata from files

* The MANAGER OF RESULTS API specifies the management and handle of query results 


### HISTOGRAMS

***Interfaces***

* *api/.../semagow/api/* → Histogram: basic interface of an histogram consisted of Rectangles
* *api/.../semagow/api/*  → Rectangle: multidimensional bounding box
* *api/.../semagow/api/* → STHistogram: a Self Tuning Histogram – type of <Histogram>


***Implementations***

<p>Implementation of STHistogram: </p>
<ul>
<li> *stholes-prefix/.../semagrow/stholes/* → STHistogramBase
	<p>--> contains refine(): refines/updates the histogram by using a query feedback records (**QueryRecord**)</p> </li>

<li> *stholes/../stholesOrig/* → STHolesOrigHistogram: original with enumeration ranges</li>
<li> *stholes-prefix/.../semagrow/stholes/* → STHolesHistogram: other type of ranges – n-dimensional (based on STHistogramBase)</li>
<li> *rdf/.../semagrow/rdf/* → RDFSTHolesHistogram: contains RDFRectangle - 3D ranges of subject, predicate, object (extends STHolesHistogram because of the 3D range)</li>
</ul>

###REFINE FEEDBACK

***Interfaces***

* *api/.../semagrow/api/qfr/*  → QueryRecord: a query feedback record that contains the query and its resultset (**QueryResult**)
* *api/.../semagrow/api/qfr/* → QueryResult: the resultset as a list of Rectangles

***Implementations***

<p> Implementation of QueryRecord:</p>
<ul>
<li> *qfr/.../semagrow/qfr/* → QueryRecordAdapter: based on the metadata file, it gets from the suitable result-File (with the use of **FileManager**) the query results – patterns. Take in mind that only single-pattern queries are supported. Then, it computes the RDFRectangle by using this pattern. The computation of the RDFRectangle denotes the computation of its 3 ranges – one for subject that can only contain prefixes (URI), the other for predicate that can be either prefixes or literals and the last one for objects, that can take many different forms</li>
</ul>

<p>Implementation of  QueryResult:</p>
<ul>
<li> *qfr/.../semagrow/qfr/QueryRecordAdapter.java* → QueryResultImpl: based on the resultset it contains the RDFRectangle with some statistics</li>
</ul>


###HANDLE OF METADATA

***Interfaces***

* *qfr/.../semagrow/api/* → QueryLogRecord: interface for metadata
* *qfr/.../semagrow/api/* → QueryLogHandler: interface to handle/write the log file
* *qfr/.../semagrow/api/* → QueryLogParser: interface to parse/read the log file
* *qfr/.../semagrow/api/* → QueryLogFactory: returns a  QueryLogHandler instance that will write to the supplied output stream.


***Implementations***

<p> Implementation of QueryLogRecord:</p>
<ul>
<li> *qfr/.../semagrow/impl/* → QueryLogRecordImpl: implements a QueryLogRecord</li>
<li> *qfr/.../semagrow/impl/serial/* → SerialQueryLogRecord: it is used for serialization of a QueryLogRecord (for read and write from/to the supplied input/output stream)</li>
</ul>

<p> Implementation of QueryLogHandler:</p>
<ul>
<li> *qfr/.../semagrow/impl/rdf/* → RDFQueryLogHandler: handles/writes a QueryLogRecord object in RDF format – as triples to the supplied output stream of log file</li>
<li> *qfr/.../semagrow/impl/serial/* → SerialQueryLogHandler: writes a SerialQueryLogRecord to the supplied output stream of the log file</li>
</ul>

<p> Implementation of QueryLogParser:</p>
<ul>
<li> *qfr/.../semagrow/impl/rdf/* → RDFQueryLogParser: reads an RDF model from the supplied input stream of log file and parses the info to a QueryLogRecord object</li>
<li> *qfr/.../semagrow/impl/serial/* → SerialQueryLogHandler: reads a SerialQueryLogRecord from the supplied input stream of the log file</li>
</ul>

<p> Implementation of QueryLogFactory:</p>
<ul>
<li> *qfr/.../semagrow/impl/rdf/* → RDFQueryLogFactory: returns a RDFQueryLogHandler for writing, based on RDFWriter</li>
<li> *qfr/.../semagrow/impl/serial/* → SerialQueryLogFactory: returns a SerialQueryLogHandler that will write to the provided output stream</li>
</ul>


###MANAGER OF RESULTS

***Interfaces***

* *qfr/.../semagrow/file/* → ResultMaterializationManager: interface to store and get Results
* *qfr/.../semagrow/file/* → MaterializationHandle: interface for handling the results


***Implementations***

<p> Implementation of ResultMaterializationManager:</p>
<ul>
<li> *qfr/.../semagrow/file/* → FileManager: 
		
	<p>-> getResults(): get the results from a specified input stream. The parsing uses a queue to load a small part at a time (consumer-producer)</p>
	<p>-> saveResults(): returns a StoreHandler with a supplied output stream of the result file</p>
</li>
</ul>

<p> Implementation of  MaterializationHandle:</p>
<ul>
<li> *qfr/.../semagrow/file/FileManager.java* → StoreHandler: handles/commits the results through endQueryResults() function</li>
</ul>


