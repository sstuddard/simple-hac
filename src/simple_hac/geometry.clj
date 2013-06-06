(ns simple-hac.geometry
  (:require [simple-hac.sparsevector :as vectorz]
            [simple-hac.util :as util]
            [clojure.set :as sets]))

(defn euclidean-distance
  "Return the Euclidean distance between two sparse vectors"
  [x y]
  (let [a (vectorz/union-sparse-index x y)]
    (letfn [(square [z] (* z z)) ]
      (reduce + (map #(square (- (vectorz/sparse-array-value x %) (vectorz/sparse-array-value y %))) a))))) 

(defn jaccard-index
  "Return the Jaccard index between two sparse vectors"
  [x y]
  (let [x-indices (set (keys x))
        y-indices (set (keys y))]
    (/ (count (sets/intersection x-indices y-indices)) (count (sets/union x-indices y-indices)))))

(defn jaccard-distance
  "Return the Jaccard distance between two sparse vectors"
  [x y]
  (- 1 (double (jaccard-index x y))))

(defn dot-product
  "Returns the dot-product of two sparse vectors"
  [v1 v2]
  (let [indices (vectorz/union-sparse-index v1 v2)]
    (reduce + (map #(reduce * (vectorz/sparse-array-values [v1 v2] %)) indices))))

(defn calculate-centroid
  "Calculate new centroid based on a list of vectors"
  [v]
  (let [n (count v)]
    (util/remap (reduce vectorz/add-vectors v) #(/ % n))))

(defn nearest-neighbor
  "Returns the vector closest to the specified target vector"
  [vectors target]
  (first (util/least-by #(second %) (for [v vectors] [v (euclidean-distance v target)]))))

