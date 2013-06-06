(ns simple-hac.sparsevector
  (:use [clojure.set]
        [simple-hac.util])
  (:require [clojure.math.numeric-tower :as math]))

(defn union-sparse-index
  "Given a list of sparse vectors, return the set of indices"
  [& v]
  (reduce union (map #(set (keys %)) v)))

(defn sparse-array-value 
  "Returns the index value of the sparse vector, or 0"
  [m i] 
  (or (m i) 0))

(defn sparse-array-values 
  "Returns the indexed values of the sparse vectors"
  [v i]
  (map #(sparse-array-value % i) v))

(defn vector-length
  "Returns the length of a sparse vector"
  [v]
  (math/sqrt (reduce + (map #(* %1 %1) (vals v)))))

(defn add-vectors
  "Adds two sparse vectors"
  [v1 v2]
  (let [indices (set (concat (keys v1) (keys v2)))]
    (into {} (map #(hash-map % (+ (get v1 % 0) (get v2 % 0))) indices))))

(defn normalize
  "Normalizes a sparse vector to unit length"
  [v]
  (let [length (vector-length v)]
    (remap v #(/ % length))))

  
