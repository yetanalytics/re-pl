(ns re-pl.repl
  (:require
   [replumb.core :as replumb]
   [replumb.browser :as browser]))

(defn read-eval-call [result-cb user-str]
  (replumb/read-eval-call
   browser/default-opts
   result-cb
   user-str))
