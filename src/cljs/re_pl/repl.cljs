(ns re-pl.repl
  (:require
   [replumb.core :as replumb]
   [replumb.browser :as browser]
   [replumb.load :as load]
   [replumb.ast :as ast]
   [replumb.repl :refer [st current-ns]]
   ))

(defn read-eval-call [result-cb user-str]
  (replumb/read-eval-call
   (assoc
    browser/default-opts
    :load-fn! load/fake-load-fn!) ;; we don't load nuthin.
   result-cb
   user-str))

(defn get-prompt []
  (replumb/get-prompt))

(defn autocomplete-terms []
  (let [state @st
        nss (ast/known-namespaces state)]

    (into
     []
     (apply
      concat
      (for [ns nss]
        (into [(str ns)]
              (map (comp str first))
              (ast/ns-defs state ns)))))))
