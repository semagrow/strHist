1) HISTOGRAMS
-------------

- api:
	<p>*api/.../semagow/api/* → **Histogram**: basic interface of an histogram consisted of Rectangles<p>
	<p>*api/.../semagow/api/* → **Rectangle**: multidimensional bounding box<p>
	<p>*api/.../semagow/api/* → **STHistogram**: a Self Tuning Histogram – type of <Histogram><p>

- implementation of STHistogram: 
	<p>*stholes-prefix/.../semagrow/stholes/* → **STHistogramBase**<p>
	--> contains refine(): refines/updates the histogram by using a query feedback records (**QueryRecord**) 

	<p>*stholes/../stholesOrig/* → **STHolesOrigHistogram**: original with enumeration ranges<p>
	<p>*stholes-prefix/.../semagrow/stholes/* → **STHolesHistogram**: other type of ranges – n-dimensional (based on STHistogramBase)<p>
	<p>*rdf/.../semagrow/rdf/* → **RDFSTHolesHistogram**: contains RDFRectangle - 3D ranges of subject, predicate, object (extends STHolesHistogram because of the 3D range)<p>

2) REFINE FEEDBACK
------------------

- api:
	<p>*api/.../semagrow/api/qfr/* → **QueryRecord**: a query feedback record that contains the query and its resultset (**QueryResult**)<p>
	<p>*api/.../semagrow/api/qfr/* → **QueryResult**: the resultset as a list of Rectangles<p>

- implementation of QueryRecord:
	<p>*qfr/.../semagrow/qfr/* → **QueryRecordAdapter**: based on the metadata file, it gets from the suitable result-File (with the use of **FileManager**) the query results – patterns. Take in mind that only single-pattern queries are supported. Then, it computes the RDFRectangle by using this pattern. The computation of the RDFRectangle denotes the computation of its 3 ranges – one for subject that can only contain prefixes (URI), the other for predicate that can be either prefixes or literals and the last one for objects, that can take many different forms<p>

-implementation of  QueryResult:
	<p>*qfr/.../semagrow/qfr/QueryRecordAdapter.java* → **QueryResultImpl**: based on the resultset it contains the RDFRectangle with some statistics<p>


3) HANDLE OF METADATA
---------------------

- api:
	<p>*qfr/.../semagrow/api/* → **QueryLogRecord**: interface for metadata<p>
	<p>*qfr/.../semagrow/api/* → **QueryLogHandler**: interface to handle/write the log file<p>
	<p>*qfr/.../semagrow/api/* → **QueryLogParser**: interface to parse/read the log file<p>
	<p>*qfr/.../semagrow/api/* → **QueryLogFactory**: returns a  QueryLogHandler instance that will write to the supplied output stream<p>

- implementation of QueryLogRecord:
	<p>*qfr/.../semagrow/impl/* → **QueryLogRecordImpl**: implements a QueryLogRecord<p>
	<p>*qfr/.../semagrow/impl/serial/* → **SerialQueryLogRecord**: it is used for serialization of a QueryLogRecord (for read and write from/to the supplied input/output stream)<p>

- implementation of QueryLogHandler:
	<p>*qfr/.../semagrow/impl/rdf/* → **RDFQueryLogHandler**: handles/writes a QueryLogRecord object in RDF format – as triples to the supplied output stream of log file<p>
	<p>*qfr/.../semagrow/impl/serial/* → **SerialQueryLogHandler**: writes a SerialQueryLogRecord to the supplied output stream of the log file<p>

- implementation of QueryLogParser:
	<p>*qfr/.../semagrow/impl/rdf/* → **RDFQueryLogParser**: reads an RDF model from the supplied input stream of log file and parses the info to a QueryLogRecord object<p>
	<p>*qfr/.../semagrow/impl/serial/* → **SerialQueryLogHandler**: reads a SerialQueryLogRecord from the supplied input stream of the log file<p>

- implementation of QueryLogFactory:
	<p>*qfr/.../semagrow/impl/rdf/* → **RDFQueryLogFactory**: returns a RDFQueryLogHandler for writing, based on RDFWriter<p>
	<p>*qfr/.../semagrow/impl/serial/* → **SerialQueryLogFactory**: returns a SerialQueryLogHandler that will write to the provided output stream<p>


4) MANAGER OF RESULTS
---------------------

- api:
	<p>qfr/.../semagrow/file/ → ResultMaterializationManager: interface to store and get results<p>
	<p>qfr/.../semagrow/file/ → MaterializationHandle: interface for handling the results<p>

- implementation of ResultMaterializationManager:
	<p>qfr/.../semagrow/file/ FileManager: 
		
	-> getResults(): get the results from a specified input stream. The parsing uses a queue to load a small part at a time (consumer-producer)
	-> saveResults(): returns a StoreHandler with a supplied output stream of the result file<p>

- implementation of  MaterializationHandle
	qfr/.../semagrow/file/FileManager.java → StoreHandler: handles/commits the results through endQueryResults() function


