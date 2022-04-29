(ns de.zalf.berest.client.hoplon.util
  (:require [cljs-time.core :as cstc]
            [cljs-time.format :as cstf]
            [cljs-time.coerce :as cstcoe]
            [hoplon.core :as hc]))

(defn cell-update-in
  [global-cell path-to-substructure]
  (fn [path func & args]
    (apply update-in global-cell (vec (concat path-to-substructure path)) func args)))

(defn round [value & {:keys [digits] :or {digits 0}}]
  (let [factor (.pow js/Math 10 digits)]
    (-> value
        (* ,,, factor)
        (#(.round js/Math %))
        (/ ,,, factor))))

(defn js-date-time->date-str [date]
  (some-> date .toJSON (.split ,,, "T") first))

(defn doy->cljs-time-date
  [doy & [year]]
  (cstc/plus (cstc/date-time (or year 2010) 1 1) (cstc/days (dec doy))))

(defn doy->js-date
  [doy & [year]]
  (cstcoe/to-date (doy->cljs-time-date doy year)))

(defn cljs-time-date->doy
  "get day of year (doy) from a js/Date"
  [date]
  (cstc/in-days (cstc/interval (cstc/date-time (cstc/year date) 1 1)
                               (cstc/plus date (cstc/days 1)))))

(defn js-date->doy
  "get day of year (doy) from a js/Date"
  [js-date]
  (cljs-time-date->doy (cstcoe/from-date js-date)))

(defn dmy-date->doy
  "get day of year (doy) either from a date with its constituents
  or from a js/Date"
  [day month & [year]]
  (cstc/in-days (cstc/interval (cstc/date-time (or year 2010) 1 1)
                               (cstc/plus (cstc/date-time (or year 2010) month day) (cstc/days 1)))))

(defn cljs-time-date->iso-y-m-d
  [date]
  (cstf/unparse (cstf/formatters :date) date))

(defn cljs-time-date->german-d-m-y
  [date]
  (cstf/unparse (cstf/formatter "dd.MM.yyyy") date))

(defn doy->german-d-m-y
  [doy & [year]]
  (cljs-time-date->german-d-m-y (doy->cljs-time-date doy year)))

(defn is-leap-year [year]
  (= 0 (rem (- 2012 year) 4)))

(def indexed (partial map-indexed vector))
#_(defn indexed [col]
    (->> col
         (interleave (range))
         (partition 2)))

(defn val-event [event]
  (-> event .-target .-value))

(def sum (partial reduce + 0))

(defn by-id [id] (.getElementById js/document (name id)))

(defn val-id [id] (hc/do! (by-id id) :value))


(defn get-chart [chart-id]
  (let [win js/window
        hc (.-Highcharts win)
        charts (.-charts hc)
        check-id (fn [c] (do (println "chart-id: " chart-id " -> " (= (str (.-id (.-renderTo c))) (str chart-id)) " -> " (-> c .-renderTo .-id (= ,,, chart-id))) (-> c .-renderTo .-id str (= ,,, (str chart-id)))))
        chart' (filter check-id charts) #_(.filter charts check-id)
        _ (println "charts: " charts " chart-id: " chart-id " chart': " chart')]
    (when (and chart' (seq chart'))
      (first chart'))))

(defn apply-fn-to-series [chart-id series-index f]
  (when-let [chart (get-chart chart-id)]
    (let [series (.-series chart)]
      (when (> (count series) series-index)
        (let [curve (aget chart series-index)]
          (f curve))))))