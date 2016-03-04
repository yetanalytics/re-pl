(ns re-pl.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]
            [re-pl.editor :refer [repl-editor display]]))

(defn main-panel []
  (let [buffer-str (re-frame/subscribe [:buffer])
        prompt (re-frame/subscribe [:prompt])
        dump (re-frame/subscribe [:debug/dump])]
    (fn []
      [:div.cm-no-pad
       [:div.re-pl-display
        [display @buffer-str]]
       [:div.re-pl-input
        [:div.re-pl-prompt
         [display @prompt]]
        [repl-editor
         (fn [code]
           (re-frame/dispatch
            [:read-eval-call code]))]]])))
