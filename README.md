# simple-hac

A simple hierarchical agglomerative clustering implementation in Clojure.  This is not industrial strength or performant.  

While the clustering is generalized to sparse vectors (represented as maps), the executable is built to cluster text documents.  Tokenization is naive, no stemming.  This implementation supports several variants, including single link, complete link, centroid, medoid, and average link clustering.

## Usage

    $ java -jar simple-hac-0.1.0-standalone.jar [args]

## Options

    ["-f" "--file"]
Defines the input file to use, one line per document

    ["-i" "--idf-file"]
Defines an external idf file, with each line containing a whitespace separated term and weight

    ["-d" "--documentkey"]
Indicates the first token on a line is the identifier of the document, otherwise they are numbered

    ["-c" "--complete-link"]
Output complete link clustering

    ["-s" "--single-link"]
Output single link clustering

    ["-a" "--average-link"]
Output average link clustering

    ["-x" "--centroid-link"]
Output centroid link clustering

    ["-m" "--medoid-link"]
Output medoid link clustering (nearest neighbor to centroid)

    ["-t" "--similarity-threshold"]
To limit the clustering to a similarity threshold

    ["-v" "--verbose"]
Verbose loggin

## Examples

    -f "./my_corpus.txt" -i "./my_idf.txt" -c -a -t 0.01 -d 

Using the file my_corpus.txt and idf my_idf.txt, output clusterings with complete link and average link strategies, stopping clustering at a similarity threshold of 0.01.  The first token in each document is an identifier.

## License

Copyright © 2013 Shayne Studdard

Distributed under the Eclipse Public License, the same as Clojure.
