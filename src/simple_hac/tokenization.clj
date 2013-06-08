(ns simple-hac.tokenization
  (:use [clojure.set]
        [simple-hac.sparsevector]))

(defn stopword? [word]
  (let [stopwords [ "the" "he" "she" "it" "and" "or" "if" "but" "i" "is" "me" "can" "of" "as" "to" "a" "in"
                    "by" "are" "we" "was" "this" "for" "that" "these" "be" "than" "then" "from" "an" "his" "hers"
                    "her" "with" "says" "they" "on" "got" "what" "do" "there" "so" "has" "you" "who" "have"
                    "had" "how" "our" "were" "my"]]
    (some #(= word %) stopwords)))

(defn tokenize
  "Tokenizes a string into terms"
  [text]
  (let [filtered (clojure.string/replace text "\\n" " ")]
    (re-seq #"\w+" filtered)))

(defn termify
  "Filters tokens, removing stopwords and lowercasing."
  [tokens]
  (filter #(not (stopword? %)) 
    (map clojure.string/lower-case tokens)))

(defn get-vocabulary 
  "Generates a set that is the vocabulary of the documents"
  [documents]
  (vec (reduce union (map set documents))))

(defn vocab-term-lookup
  "Generates term to index lookup for vocabulary"
  [vocabulary]
  (zipmap vocabulary (range)))

(defn vocab-index-lookup
  "Generates index to term lookup for vocabulary"
  [vocabulary]
  (zipmap (range) vocabulary))

(defn get-term-vector
  "Given a list of terms, weights, and a vocabulary term lookup, create a sparse vector"
  [term-weights term-lookup]
    (reduce (fn [new-map [k v]] (assoc new-map (term-lookup k) v)) {} term-weights))

(defn get-term-frequency-vector
  "Given a list of terms and a vocabulary term lookup, create a sparse term vector"
  [terms term-lookup]
  (get-term-vector (frequencies terms) term-lookup))

(defn get-tfidf-vector
  "Given a tf vector and idf function, apply idf weighting"
  [v gidf-fn]
  (apply merge
    (map (fn [[k v]] 
            (if (gidf-fn k)
              {k (* v (gidf-fn k))}
              {}))
      v)))

(defn idf
  "Generate idf lookup from a set of documents"
  [documents]
  (let [vocabulary (get-vocabulary documents)]
    (zipmap vocabulary
      (for [term vocabulary] 
        (java.lang.Math/log (/ (count documents) 
          (reduce + 
            (for [doc documents] (if (some #{term} doc) 1 0)))))))))

(defn idf-fn 
  "Generates an idf lookup fn"
  [documents idf-table]
  (let [vocabulary (get-vocabulary documents)
        term-lookup (vocab-index-lookup vocabulary)]
    (fn [term]
      (idf-table (term-lookup term)))))
