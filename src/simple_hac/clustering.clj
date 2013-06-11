(ns simple-hac.clustering
  (:require [simple-hac.sparsevector :as vectorz]
            [simple-hac.geometry :as geo]
            [simple-hac.util :as util]))

(def calculate-centroid (memoize geo/calculate-centroid))
(def euclidean-distance (memoize geo/euclidean-distance))
(def nearest-neighbor (memoize geo/nearest-neighbor))

(defn similarity-calculator
  "A specialized similarity calculator that will memoize bi-directional pairs"
  []
  (let [mem (atom {})]
    (fn [v1 v2]
      (if-let [e (find @mem [v1 v2])]
        (val e)
        (let [similarity (geo/dot-product v1 v2)]
          (swap! mem assoc [v1 v2] similarity [v2 v1] similarity)
          similarity)))))
 
(defn single-link-clustering 
  "Returns a comparator function for single link agglomerative clustering"
  []
  (let [calc (similarity-calculator)]
    (fn [c1 c2]
      (util/greatest-by identity 
        (for [x c1 y c2] (calc x y))))))
     
(defn complete-link-clustering 
  "Returns a comparator function for complete link agglomerative clustering"
  []
  (let [calc (similarity-calculator)]
    (fn [c1 c2]
      (util/least-by identity 
        (for [x c1 y c2] (calc x y))))))
 
 (defn average-link-clustering 
  "Returns a comparator function for average link agglomerative clustering"
  []
  (let [calc (similarity-calculator)]
    (fn [c1 c2]
      (let [num-compares (* (count c1) (count c2))]
        (/ (reduce + (for [x c1 y c2] (calc x y))) num-compares)))))

 (defn centroid-link-clustering
  "Returns a comparator function for centroid link agglomerative clustering"
  []
  (let [calc (similarity-calculator)]
    (fn [c1 c2]
      (let [c1-centroid (calculate-centroid c1)
            c2-centroid (calculate-centroid c2)]
        (calc c1-centroid c2-centroid)))))

 (defn medoid-link-clustering
  "Returns a comparator function for medoid (member nearest to centroid) link agglomerative clustering"
  []
  (let [calc (similarity-calculator)]
    (fn [c1 c2]
      (let [m1 (nearest-neighbor c1 (calculate-centroid c1))
            m2 (nearest-neighbor c2 (calculate-centroid c2))]
        (calc m1 m2)))))
 
(defn cluster-next
  "Performs a single step of the hac clustering, searching for the highest similarity as returned by f"
  [clusters f]
  (let [cluster-pairs (for [x clusters y (rest (drop-while #(not= x %) clusters))] [x y])
        similarities (for [[c1 c2] cluster-pairs] [c1 c2 (f c1 c2)])
        [c1 c2 similarity] (util/greatest-by #(nth % 2) similarities)
        new-cluster (into [] (concat c1 c2))
        result-clusters (into [new-cluster] (filter #(and (not= c1 %) (not= c2 %)) clusters))]
    [result-clusters c1 c2 similarity]))

(defn cluster
  "Returns a lazy sequence that progressively clusters one pair at a time base on highest similary
  as defined by f"
  [clusters f]
  (if (= 1 (count clusters))
    nil
    (let [next-clustering (cluster-next clusters f)
          new-cluster (first next-clustering)]
      (cons next-clustering (lazy-seq (cluster new-cluster f))))))

