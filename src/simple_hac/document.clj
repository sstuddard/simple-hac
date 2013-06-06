(ns simple-hac.document)

(defn document-lookup
  "Build a document lookup given the vectors and keys"
  [v k]
  (zipmap v k))

(defn format-term
  "Prints a term and value"
  [t v l]
  (format "(%s: %.5f)" (l t) (double v)))

(defn format-doc-vector
  "Print a document vector with a lookup"
  [d l]
  (let [sorted (into (sorted-map-by (fn [key1 key2]
                         (compare [(get d key2) key2]
                                  [(get d key1) key1]))) d)]
    (map #(format-term (first %1) (second %1) l) sorted)))

