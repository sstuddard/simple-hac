(ns simple-hac.core
  (:gen-class)
  (:require [simple-hac.tokenization :as token]
           [simple-hac.sparsevector :as vectorz]
           [simple-hac.document :as document]
           [simple-hac.clustering :as clustering]
           [simple-hac.util :as util])
  (:use [clojure.tools.cli :only [cli]]))

(defn build-data
  "Given a file, prepares all the data necessary for clustering."
  [filepath document-key-included]
  (let [data (util/get-lines filepath)
        tokenized (map token/tokenize data)
        documents (map token/termify (if document-key-included (map rest tokenized) tokenized))
        vocabulary (token/get-vocabulary documents)
        gidf (token/idf-fn documents)
        index-lookup (token/vocab-term-lookup vocabulary)
        term-lookup (token/vocab-index-lookup vocabulary)
        raw-vectors (map #(token/get-term-frequency-vector % index-lookup) documents)
        vectors (map #(vectorz/normalize (token/get-tfidf-vector % gidf)) raw-vectors)
        doc-lookup (document/document-lookup vectors (if document-key-included (map first tokenized) (range)))
        ]
  {
    :data data
    :tokenized tokenized
    :documents documents
    :vocabulary vocabulary
    :gidf gidf
    :term-to-index index-lookup
    :raw-vectors raw-vectors
    :vectors vectors
    :index-to-term term-lookup
    :doc-lookup doc-lookup
  }))

(defn print-cluster-iteration
  [[clusters c1 c2 similarity] lookup]
  (println (map lookup c1) " + " (map lookup c2) " => " similarity))

(defn get-clustering-strategies
  "Returns a sequence of [description function] clustering strategies based on options."
  [options]
  (let [strategies (filter options (keys (select-keys options [:single-link :complete-link :average-link :centroid-link :medoid-link])))]
    (map #(cond
            (= :single-link %) ["Single link clustering" (clustering/single-link-clustering)]
            (= :complete-link %) ["Complete link clustering" (clustering/complete-link-clustering)]
            (= :average-link %) ["Average link clustering" (clustering/average-link-clustering)]
            (= :centroid-link %) ["Centroid link clustering" (clustering/centroid-link-clustering)]
            (= :medoid-link %) ["Medoid link clustering" (clustering/medoid-link-clustering)]
            :else nil) strategies)))

(defn -main
  "Run hierarchical agglomerative clustering on documents from a line-delimited file. 
   If specififed, the first token of a line is the identifier for the document."
  [& args]
  (let [opts (cli args
;               ["-k" "--clusters" "Specify number of clusters" :parse-fn #(Integer. %) :default 2] 
               ["-f" "--file" "Input file"]
               ["-s" "--single-link" "Use single link agglomeration clustering" :flag true :default false]
               ["-c" "--complete-link" "Use complete link agglomeration clustering" :flag true :default false]
               ["-a" "--average-link" "Use average link agglomeration clustering" :flag true :default false]
               ["-x" "--centroid-link" "Use centroid link agglomeration clustering" :flag true :default false]
               ["-m" "--medoid-link" "Use medoid (member nearest to centroid) link agglomeration clustering" :flag true :default false]
               ["-d" "--documentkey" "The first token of a document is its key" :flag true :default false]
;               ["-c" "--centroids" "Output centroids" :flag true]
;               ["-j" "--jaccard" "Use Jaccard distance" :flag true]
;               ["-v" "--verbose" "Verbose output" :flag true]
;               ["-n" "--randomruns" "Run n times per centroids selection" :parse-fn #(Integer. %) :default 1]
;               ["-m" "--iterations" "The max iterations for convergence" :parse-fn #(Integer. %) :default 10])
      )]
    (let [options (first opts)
          filepath (options :file)
          verbose (options :verbose)
          document-key-included (options :documentkey)
          build (build-data filepath document-key-included) 
          vectors (:vectors build)
          vector->doc-id (:doc-lookup build)
          initial-clusters (map #(vector %) vectors)
          strategies (get-clustering-strategies options)]
      (doseq [[description strategy] strategies]
        (let [cluster-seq (clustering/cluster initial-clusters strategy)]
          (println description)
          (doseq [c cluster-seq]
            (print-cluster-iteration c vector->doc-id))
          (println " "))))))
