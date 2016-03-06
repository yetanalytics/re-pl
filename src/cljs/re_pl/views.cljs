(ns re-pl.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]))


(def console
  (r/create-class
   {:reagent-render
    (fn []
      [:textarea
       {:default-value ""
        :auto-complete "off"
        }])
    :component-did-mount
    (fn [this]
      (re-frame/dispatch [:console/init
                          (r/dom-node this)
                          {}]))}))

(defn main-panel []
  [:div
   [console]])
