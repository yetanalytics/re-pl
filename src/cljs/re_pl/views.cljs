(ns re-pl.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]
            [re-pl.config :refer [debug?]]))


(defonce welcome-text
  (apply
   str
   (into []
         (interpose "\n")
         [";; Welcome to re-pl!"
          ";;"
          ";; Key Bindings:"
          ";;   Enter: evaluate!"
          ";;   Ctrl-Up/Down: history"
          ";;   Tab: autocomplete"
          ";;   Other: CodeMirror Emacs bindings"
          " "])))

(def console
  (r/create-class
   {:reagent-render
    (fn []
      [:textarea
       (cond-> {:default-value
                welcome-text
                :auto-complete "off"}
         (not debug?) (assoc :style {:display "none"}))])
    :component-did-mount
    (fn [this]
      (re-frame/dispatch [:console/init
                          (r/dom-node this)
                          {}]))}))

(defn main-panel []
  (let [dump (re-frame/subscribe [:debug/dump])]
    (fn []
      [console])))
