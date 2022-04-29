(ns de.zalf.berest.client.hoplon.state
  (:require-macros [javelin.core :refer [prop-cell cell=]])
  (:require [clojure.string :as str]
            [javelin.core :as j :refer [cell]]
            [castra.core  :as c]))

#_(enable-console-print!)

(def server-url #_(condp = (-> js/window .-location .-hostname)
                  "" "http://localhost:3000/"
                  "localhost" "http://localhost:3000/"
                  "http://irrigama-web.elasticbeanstalk.com/")
  "http://localhost:3000/"
  #_"http://irrigama-web.elasticbeanstalk.com/")
#_(println "server-url: " server-url)

(enable-console-print!)

;stem cell
(def state (cell {}))
#_(cell= (println "state: " (pr-str state)))

(def pwd-update-success? (cell nil))
(def climate-data-import-time-update-success? (cell nil))
(def climate-data-import-success? (cell nil))

;cell holding static app state, which will hardly change
(def static-state (cell nil))
#_(cell= (println "static-state:\n " (pr-str static-state)))

(def slopes (cell= (:slopes static-state)))
(def stts (cell= (:stts static-state)))
(def substrate-groups (cell= (into {} (map (juxt :soil.substrate/key identity) (:substrate-groups static-state)))))
(def ka5-soil-types (cell= (into {} (map (juxt :soil.type.ka5/name identity) (:ka5-soil-types static-state)))))
(def crop->dcs (cell= (:crop->dcs static-state)))
#_(defc= stt-descriptions (into {} (map (juxt :soil.stt/key :soil.stt/description) stts)))
(def minimal-all-crops (cell= (:minimal-all-crops static-state)))
(def all-weather-stations (cell= (:all-weather-stations static-state)))

;local state
(def weather-station-data (cell {}))
#_(cell= (println "weather-station-data: " (pr-str weather-station-data)))

(def crop-state (cell nil))
#_(cell= (when crop-state (println "crop-state: " (pr-str crop-state))))

(def processed-crop-data (cell= (:processed crop-state)))
(def raw-crop-data (cell= (:raw crop-state)))
#_(defc= user-crop? (= (:crop-type crop-state) :user))

(def breadcrumbs (cell {:farm nil
                        :plot nil
                        :plot-annual nil
                        :weather-station nil
                        :weather-year nil
                        :crop nil}))
#_(cell= (println "breadcrumbs: " (pr-str breadcrumbs)))

(defn set-breadcrumb
  [key value]
  (swap! breadcrumbs assoc key value))

;derived state

(def farms (cell= (:farms state)))

(def users (cell= (:users state)))

(def user-weather-stations (cell= (:weather-stations state)))
#_(cell= (println "user-weather-stations: " (pr-str user-weather-stations)))

(def technology-cycle-days (cell= (-> state :technology :technology/cycle-days)))
(defn set-technology-cycle-days
  [value]
  (swap! state update-in [:technology :technology/cycle-days] value))

(def technology-outlet-height (cell= (-> state :technology :technology/outlet-height)))
(defn set-technology-outlet-height
  [value]
  (swap! state update-in [:technology :technology/outlet-height] value))

;copied from old javelin version, as the old one had more properties
;especially it used prop-cell which has been used here to manipulate the
;browser url more easily
(defn route-cell
  "Manage the URL hash via Javelin cells. There are three arities:
  - When called with no arguments this function returns a formula cell whose
    value is the URL hash or nil.
  - When called with a single string argument, the argument is taken as the
    default value, which is returned in place of nil when there is no hash.
  - When a single cell argument is provided, the URL hash is kept synced to the
    value of the cell.
  - When a cell and a callback function are both provided, the URL hash is kept
    synced to the value of the cell as above, and any attempt to change the hash
    other than via the setter cell causes the callback to be called. The callback
    should be a function of one argument, the requested URL hash."
  ([]
   (let [r (prop-cell (.. js/window -location -hash))]
     (cell= (when (not= "" r) r))))
  ([setter-or-dfl]
   (if (j/cell? setter-or-dfl)
     (prop-cell (.. js/window -location -hash) setter-or-dfl)
     (let [r (route-cell)] (cell= (or r setter-or-dfl)))))
  ([setter callback]
   (prop-cell (.. js/window -location -hash) setter callback)))

(def routeHash (cell (.. js/window -location -hash)))
#_(cell= (println "routeHash: " (pr-str routeHash)))
(def full-route (route-cell routeHash #(reset! routeHash %)))
(def route+params (cell= (str/split full-route #"\?|\&|=")))
#_(cell= (println "route+params: " (pr-str route+params)))
(def route (cell= (first route+params)))
#_(cell= (println "route: " (pr-str route)))
(def route-params (cell= (into {} (for [[k v] (partition 2 (rest route+params))]
                                    [(keyword k) v]))))
#_(cell= (println "route-params: " (pr-str route-params)))

(def route-params-str
  (cell= (->> route-params
              (map (fn [[k v]] (when v (str (name k) "=" v))) ,,,)
              (remove nil? ,,,)
              (str/join "&" ,,,))))
#_(cell= (println "route-params-str: " (pr-str route-params-str)))

(defn clear-route+params
  []
  (reset! routeHash ""))

(defn set-route+params
  [& [route* & params]]
  (->> (merge @route-params (into {} (for [[k v] (partition 2 params)] [k v])))
       (map (fn [[k v]] (when v (str (name k) "=" v))) ,,,)
       (remove nil? ,,,)
       (str/join "&" ,,,)
       (str (or route* @route) "?" ,,,)
       (reset! routeHash ,,,)))

(defn clear-route-params
  []
  #_(println "route: " (pr-str @route) " route-params: " (pr-str @route-params)
           " map: " (pr-str (map (fn [[k _]] [k nil]) @route-params))
           " flatten: " (pr-str (flatten (map (fn [[k _]] [k nil]) @route-params))))
  (apply set-route+params @route (flatten (map (fn [[k _]] [k nil]) @route-params))))

(defn set-route-params
  [& params]
  (apply set-route+params nil params))

(defn set-route
  [route*]
  (set-route+params route*))

(def error (cell nil))
(def loading (cell []))
#_(cell= (println "(count loading) = " (count loading)))

(def csv-result (cell nil))
#_(cell= (println "csv-result: " (pr-str csv-result)))
(def calc-error (cell nil))
(def calculating (cell []))

(def user (cell= (:user-credentials state)))
#_(cell= (println "user-creds: " (pr-str user)))

(def lang (cell= (:language state)))
#_(cell= (println "lang: " (pr-str lang)))

(def loaded? (cell= (not= {} state)))
(def loading? (cell= (seq loading)))

(def logged-in? (cell= (not (nil? user))))
#_(cell= (println "logged-in?: "(pr-str logged-in?)))


(defn has-role
  [user role]
  (let [r (if (namespace role)
            role
            (keyword "user.role" (name role)))]
    (and (not (nil? user))
         ((:user/roles user) r))))

(defn has-user-role
  [role]
  (has-role @user role))

(def admin-logged-in? (cell= (has-role user :admin)))
(def consultant-logged-in? (cell= (has-role user :consultant)))

(def show-login?  (cell= (and #_loaded? (not logged-in?))))
#_(cell= (println "show-login?: " show-login?))

(def show-content?  (cell= (and loaded? logged-in?)))

(def clear-error!   #(reset! error nil))


(def login! (c/mkremote 'de.zalf.berest.web.castra.api/login state error loading)) ;:url server-url
(def logout! (c/mkremote 'de.zalf.berest.web.castra.api/logout state error loading))
(def get-state (c/mkremote 'de.zalf.berest.web.castra.api/get-berest-state state error loading))
#_(def get-full-selected-crops (mkremote 'de.zalf.berest.web.castra.api/get-state-with-full-selected-crops state error loading))
(def calculate-csv (c/mkremote 'de.zalf.berest.web.castra.api/calculate-csv csv-result calc-error calculating))
(def simulate-csv (c/mkremote 'de.zalf.berest.web.castra.api/simulate-csv csv-result calc-error calculating))

(defn calculate-from-db
  [result-cell plot-id until-abs-day year]
  ((c/mkremote 'de.zalf.berest.web.castra.api/calculate-from-db result-cell error loading) plot-id until-abs-day year))

(def load-static-state
  (c/mkremote 'de.zalf.berest.web.castra.api/get-static-state static-state error loading))
(cell= (when logged-in? #_(and s/logged-in? (not s/static-state))
         (load-static-state)))

;plot
(def create-new-plot (c/mkremote 'de.zalf.berest.web.castra.api/create-new-plot state error loading))
(def create-new-plot-annual (c/mkremote 'de.zalf.berest.web.castra.api/create-new-plot-annual state error loading))

;user
(def create-new-user (c/mkremote 'de.zalf.berest.web.castra.api/create-new-user state error loading))
(def set-new-password (c/mkremote 'de.zalf.berest.web.castra.api/set-new-password pwd-update-success? error loading))
(def update-user-roles (c/mkremote 'de.zalf.berest.web.castra.api/update-user-roles state error loading))

;weather-data
(def load-weather-station-data (c/mkremote 'de.zalf.berest.web.castra.api/get-weather-station-data weather-station-data error loading))
(def create-new-local-user-weather-station (c/mkremote 'de.zalf.berest.web.castra.api/create-new-local-user-weather-station state error loading))
(def add-user-weather-stations (c/mkremote 'de.zalf.berest.web.castra.api/add-user-weather-stations state error loading))
(def remove-user-weather-stations (c/mkremote 'de.zalf.berest.web.castra.api/remove-user-weather-stations state error loading))
(def import-weather-data (c/mkremote 'de.zalf.berest.web.castra.api/import-weather-data state error loading))
(def create-new-weather-data (c/mkremote 'de.zalf.berest.web.castra.api/create-new-weather-data state error loading))

;farms
(def create-new-farm (c/mkremote 'de.zalf.berest.web.castra.api/create-new-farm state error loading))
(def create-new-farm-address (c/mkremote 'de.zalf.berest.web.castra.api/create-new-farm-address state error loading))
(def create-new-farm-contact (c/mkremote 'de.zalf.berest.web.castra.api/create-new-farm-contact state error loading))

;soils
(def create-new-soil-data-layer (c/mkremote 'de.zalf.berest.web.castra.api/create-new-soil-data-layer state error loading))
(def set-substrate-group-fcs-and-pwps (c/mkremote 'de.zalf.berest.web.castra.api/set-substrate-group-fcs-and-pwps state error loading))
(def create-new-soil-moisture (c/mkremote 'de.zalf.berest.web.castra.api/create-new-soil-moisture state error loading))
(def create-new-donation (c/mkremote 'de.zalf.berest.web.castra.api/create-new-donation state error loading))
(def create-new-crop-instance (c/mkremote 'de.zalf.berest.web.castra.api/create-new-crop-instance state error loading))
(def create-new-dc-assertion (c/mkremote 'de.zalf.berest.web.castra.api/create-new-dc-assertion state error loading))

;crops
(def create-new-crop (c/mkremote 'de.zalf.berest.web.castra.api/create-new-crop static-state error loading))
(def copy-crop (c/mkremote 'de.zalf.berest.web.castra.api/copy-crop static-state error loading))
(def delete-crop (c/mkremote 'de.zalf.berest.web.castra.api/delete-crop static-state error loading))
(def load-crop-data (c/mkremote 'de.zalf.berest.web.castra.api/get-crop-data crop-state error loading))
(def update-crop-db-entity (c/mkremote 'de.zalf.berest.web.castra.api/update-crop-db-entity crop-state error loading))
(def delete-crop-db-entity (c/mkremote 'de.zalf.berest.web.castra.api/delete-crop-db-entity crop-state error loading))
(def create-new-crop-kv-pair (c/mkremote 'de.zalf.berest.web.castra.api/create-new-crop-kv-pair crop-state error loading))

;common
(def update-db-entity (c/mkremote 'de.zalf.berest.web.castra.api/update-db-entity state error loading))
(def retract-db-value (c/mkremote 'de.zalf.berest.web.castra.api/retract-db-value state error loading))
(def delete-db-entity (c/mkremote 'de.zalf.berest.web.castra.api/delete-db-entity state error loading))
(def delete-db-entities (c/mkremote 'de.zalf.berest.web.castra.api/delete-db-entity state error loading))

(def create-new-com-con (c/mkremote 'de.zalf.berest.web.castra.api/create-new-com-con state error loading))

;admin
(def set-climate-data-import-time
  (c/mkremote 'de.zalf.berest.web.castra.api/set-climate-data-import-time
            climate-data-import-time-update-success?
            error loading))

(def bulk-import-dwd-data-into-datomic
  (c/mkremote 'de.zalf.berest.web.castra.api/bulk-import-dwd-data-into-datomic
            climate-data-import-success?
            error loading))








