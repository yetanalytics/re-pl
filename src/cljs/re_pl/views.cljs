(ns re-pl.views
    (:require [re-frame.core :as re-frame]))

(defn main-panel []
  (let [buffer-str (re-frame/subscribe [:buffer])
        dump (re-frame/subscribe [:debug/dump])]
    (fn []
      [:div
       #_[:div.debug
        (str @dump)]
       [:pre
        [:code
         [:div
          @buffer-str]
         [:div
          {:contentEditable true
           :on-key-press
           (fn [e]
             (let [k (.-key e)]
               (when (= k "Enter")
                 (.preventDefault e)
                 (let [t (.-target e)]
                   ;; eval
                   (re-frame/dispatch [:read-eval-call (.-textContent t)])
                   ;; clear the div
                   (aset t "textContent" ""))
                 )))}]]]])))
