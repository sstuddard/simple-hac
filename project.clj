(defproject simple-hac "0.1.0-SNAPSHOT"
  :description "A simple Hierarchical Agglomeration Clusterer"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :jvm-opts ["-Xmx1g"]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/math.numeric-tower "0.0.2"]
                 [org.clojure/tools.cli "0.2.2"]]
  :main simple-hac.core)
