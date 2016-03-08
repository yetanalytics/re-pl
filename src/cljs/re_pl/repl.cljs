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
        nss (ast/known-namespaces state)
        current-ns (current-ns)]
    (into
     []
     (map str)

     (concat
      (-> (ast/namespace state current-ns)
          :cljs.analyzer/constants
          :seen
          seq)
      (apply
       concat
       (for [ns nss]
         (into [ns]
               (map first)
               (ast/ns-defs state ns))))))))
