(ns ^:figwheel-hooks com.yetanalytics.re-pl.core
  (:require
   cljsjs.codemirror
   cljsjs.codemirror.mode.clojure
   cljsjs.codemirror.keymap.emacs
   cljsjs.codemirror.addon.hint.show-hint
   cljsjs.codemirror.addon.edit.closebrackets
   cljsjs.codemirror.addon.edit.matchbrackets
   ;; datascript.core
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [com.yetanalytics.re-pl.handlers]
   [com.yetanalytics.re-pl.subs]
   [com.yetanalytics.re-pl.views :as views]
   [com.yetanalytics.re-pl.config :as config]
   ))



(when config/debug?
  (println "dev mode"))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (re-frame/dispatch [:update-prompt!])
  (re-frame/dispatch [:console/prompt!])
  (mount-root))

(defonce initial-load (init))

(defn ^:after-load on-reload []
  (mount-root)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
