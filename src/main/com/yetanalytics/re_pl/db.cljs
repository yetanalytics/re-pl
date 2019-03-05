(ns com.yetanalytics.re-pl.db)

(def default-db
  {:scratch "" ;; un-evaluated repl input
   :results-data (list) ;; raw repl output
   :buffer ";; Welcome to re-pl!" ;; string containing buffer as text
   :state :init ;; enum #{:init :input :eval}
   :history (list) ;; command history
   :history-pos 0
   :history-swap "" ;; holds input when traversing hist
   })
