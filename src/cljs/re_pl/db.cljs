(ns re-pl.db)

(def default-db
  {:name "re-frame"
   :scratch "" ;; un-evaluated repl input
   :results-data (list) ;; raw repl output
   :buffer "" ;; string containing buffer as text
   :history-position 0 ;; position in history
})
