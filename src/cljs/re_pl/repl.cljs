(ns re-pl.repl
  (:require
   [replumb.core :as replumb]
   [replumb.browser :as browser]
   [replumb.load :as load]))

(defn read-eval-call [result-cb user-str]
  (replumb/read-eval-call
   (assoc
    browser/default-opts
    :load-fn! load/fake-load-fn!) ;; we don't load nuthin.
   result-cb
   user-str))

(defn get-prompt []
  (replumb/get-prompt))
