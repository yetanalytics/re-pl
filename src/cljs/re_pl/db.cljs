(ns re-pl.db)

(def default-db
  {:scratch "" ;; un-evaluated repl input
   :results-data (list) ;; raw repl output
   :buffer ";; Welcome to re-pl!" ;; string containing buffer as text
   ;; :console nil
   })
