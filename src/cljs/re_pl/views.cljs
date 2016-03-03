(ns re-pl.views
    (:require [re-frame.core :as re-frame]))

(defn main-panel []
  (let [buffer-str (re-frame/subscribe [:buffer])
        prompt (re-frame/subscribe [:prompt])
        dump (re-frame/subscribe [:debug/dump])]
    (fn []
      [:div
       [:div.debug
        (str @dump)]
       [:pre
        [:code
         [:div
          @buffer-str]
         [:div
          [:span @prompt]
          [:span
           {:contentEditable true
            ;; :value @input
            :on-input (fn [e]
                        (re-frame/dispatch
                         [:set-scratch
                          (-> e .-target .-textContent)]))
            :on-key-down
            (fn [e]
              (let [k (.-keyCode e)
                    t (.-target e)]
                (case k
                  38
                  (re-frame/dispatch [:history/prev t])
                  40
                  (re-frame/dispatch [:history/next t])
                  nil)))
            :on-key-press
            (fn [e]
              (let [k (.-charCode e)]
                (when (= k 13) ;; enter
                  (.preventDefault e)
                  (let [t (.-target e)]
                    ;; eval
                    (re-frame/dispatch [:read-eval-call (.-textContent t)])
                    ;; clear the scratch buffer
                    (re-frame/dispatch
                     [:set-scratch
                      ""])
                    ;; clear the div (doesn't clear itself?)
                    (aset t "textContent" "")))))}]]]]])))
