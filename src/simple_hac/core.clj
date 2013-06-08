(ns simple-hac.core
  (:gen-class)
  (:require [simple-hac.tokenization :as token]
           [simple-hac.sparsevector :as vectorz]
           [simple-hac.document :as document]
           [simple-hac.clustering :as clustering]
           [simple-hac.util :as util])
  (:use [clojure.tools.cli :only [cli]]))

(defn prepare-data
  "Given a file, prepares all the data necessary for clustering."
  [filepath document-key-included]
  (let [data (util/get-lines filepath)
        tokenized (map token/tokenize data)
        documents (map token/termify (if document-key-included (map rest tokenized) tokenized))
        vocabulary (token/get-vocabulary documents)
        index-lookup (token/vocab-term-lookup vocabulary)
        term-lookup (token/vocab-index-lookup vocabulary)
        raw-vectors (map #(token/get-term-frequency-vector % index-lookup) documents)]
  {
    :data data
    :tokenized tokenized
    :documents documents
    :vocabulary vocabulary
    :term-to-index index-lookup
    :raw-vectors raw-vectors
    :index-to-term term-lookup
  }))

(defn print-cluster-iteration
  [[clusters c1 c2 similarity] lookup]
  (println (map lookup c1) " + " (map lookup c2) " => " similarity))

(defn print-clusters
  [[clusters c1 c2 similarity] lookup]
  (doseq [c clusters]
    (doseq [d c]
      (print (lookup d) ""))
    (println " ")))

(defn parse-idf-line
  [line]
  (let [split (clojure.string/split line #"\t+")]
    {(first split) (double (read-string (second split)))}))

(defn get-global-idf 
  [file]
  (let [lines (util/get-lines file)]
    (into {} (map parse-idf-line lines))))

(defn get-idf-fn
  [doc-vectors idf-file]
  (if idf-file
    (token/idf-fn doc-vectors (get-global-idf idf-file))
    (token/idf-fn doc-vectors (token/idf doc-vectors))))

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
               ["-f" "--file" "Input file"]
               ["-i" "--idf-file" "Term-weight external IDF file"]
               ["-s" "--single-link" "Use single link agglomeration clustering" :flag true :default false]
               ["-c" "--complete-link" "Use complete link agglomeration clustering" :flag true :default false]
               ["-a" "--average-link" "Use average link agglomeration clustering" :flag true :default false]
               ["-x" "--centroid-link" "Use centroid link agglomeration clustering" :flag true :default false]
               ["-m" "--medoid-link" "Use medoid (member nearest to centroid) link agglomeration clustering" :flag true :default false]
               ["-d" "--documentkey" "The first token of a document is its key" :flag true :default false]
               ["-t" "--similarity-threshold" "Double value of the minimum similarity" :parse-fn #(Double. %) :default 0]
               ["-v" "--verbose" "Verbose output" :flag true :default false]
      )]
    (let [options (first opts)
          filepath (options :file)
          document-key-included (options :documentkey)
          similarity-threshold (options :similarity-threshold)
          idf-file (options :idf-file)
          strategies (get-clustering-strategies options)
          multi-strategy (> 1 (count strategies))
          verbose (or (options :verbose) multi-strategy)
          displayHelp (or (nil? filepath) (empty? strategies))]
      (if displayHelp
        (println (last opts))
        (let [build (prepare-data filepath document-key-included) 
              gidf (get-idf-fn (build :documents) idf-file)
              vectors (map #(vectorz/normalize (token/get-tfidf-vector % gidf)) (build :raw-vectors))
              vector->doc-id (document/document-lookup vectors (if document-key-included (map first (build :tokenized)) (range)))
              initial-clusters (map #(vector %) vectors)]
          (doseq [[description strategy] strategies]
            (let [cluster-seq (clustering/cluster initial-clusters strategy)
                  filtered-clusters (filter #(>= (nth % 3) similarity-threshold) cluster-seq)]
              (if verbose
                (do
                  (println description)
                  (doseq [c filtered-clusters]
                    (print-cluster-iteration c vector->doc-id))
                  (println " "))
                (print-clusters (last filtered-clusters) vector->doc-id)))))))))
