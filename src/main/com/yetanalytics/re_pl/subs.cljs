(ns com.yetanalytics.re-pl.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub-raw
 :results
 (fn [db]
   (reaction (:results-data @db))))


(re-frame/reg-sub-raw
 :debug/dump
 (fn [db]
   (reaction @db)))
