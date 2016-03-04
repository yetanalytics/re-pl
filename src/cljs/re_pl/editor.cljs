(ns re-pl.editor
  (:require [reagent.core :as r]))


;; http://yogthos.net/posts/2015-11-12-ClojureScript-Eval.html


(defn editor-did-mount [eval-fn]
  (fn [this]
    (let [cm (.fromTextArea
              js/CodeMirror
              (r/dom-node this)
              #js {:mode "clojure"
                   :keyMap "emacs"
                   :theme "monokai"
                   ;; :keyMap "default"
                   :matchBrackets true
                   :autoCloseBrackets true
                   :lineNumbers false
                   :extraKeys #js {:Enter (fn [cm]
                                            (eval-fn (.getValue cm))
                                            (.setValue cm ""))}
                   })]
      (r/set-state
       this
       {:doc cm}))))


(defn repl-editor [eval-fn]
  (r/create-class
   {:render (fn []
              [:textarea
               {:default-value ""
                :auto-complete "off"
                }])
    :component-did-mount (editor-did-mount eval-fn)}))



;; Read-only repl display
(def display
  (r/create-class
   {:reagent-render (fn [input]
                      [:textarea
                       {;; :style {:height "auto"}
                        :default-value ""
                        :value input
                        :on-change (constantly nil)
                        :auto-complete "off"}])
    :component-did-mount
    (fn [this]
      (r/set-state
       this
       {:doc (.fromTextArea
              js/CodeMirror
              (r/dom-node this)
              #js {:mode "clojure"
                   :theme "monokai"
                   :readOnly "nocursor"
                   :lineNumbers false})}))
    :component-did-update
    (fn [this old-argv]
      (some-> (r/state this)
              :doc
              (.setValue (second (r/argv this)))))}))
