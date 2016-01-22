(set-env!
  :dependencies '[[adzerk/boot-cljs "1.7.170-3"]
                  [adzerk/boot-reload "0.4.2"]
                  [compojure "1.4.0"]
                  [hoplon/boot-hoplon "0.1.13"]
                  [hoplon/castra "3.0.0-alpha3"]
                  [hoplon/hoplon "6.0.0-alpha11"]
                  [org.clojure/clojure "1.7.0"]
                  [org.clojure/clojurescript "1.7.189"]
                  [pandeiro/boot-http "0.7.0"]
                  [ring "1.4.0"]
                  [ring/ring-defaults "0.1.5"]

                  [hoplon/twitter-bootstrap "0.2.0"]
                  [cljsjs/bootstrap "3.3.6-0"]
                  [cljsjs/highcharts "4.1.10-2"]
                  #_[cljsjs/jquery "1.11.3-0"]

                  [cljs-ajax "0.2.3"]

                  #_[simple-time "0.1.1"]
                  #_[clj-time "0.6.0"]
                  [com.andrewmcveigh/cljs-time "0.4.0"]
                  ]
  ;:out-path      
  :resource-paths #{"assets" "src/clj"}
  :source-paths   #{"src/cljs"
                    "src/hl"

                    ;for castra in dev mode
                    #_"../../berest-service/castra/src"
                    #_"../../berest-core/private-resources"
                    #_"../../berest-core/src"})

(require
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-reload    :refer [reload]]
  '[hoplon.boot-hoplon    :refer [hoplon prerender]]
  '[pandeiro.boot-http    :refer [serve]])

(deftask dev
         "Build berest-client for local development."
         []
         (comp
           #_(serve
             :port    8000
             :handler 'ht.handler/app
             :reload  true)
           (watch)
           (speak)
           (hoplon)
           #_(reload)
           (cljs)
           (target :dir #{"target" #_"../berest-hoplon-website/website"})
           ))

(deftask prod
         "Build berest-client for production deployment."
         []
         (comp
           (hoplon)
           (cljs :optimizations :advanced)
           (prerender)
           (target :dir #{"target" #_"../berest-hoplon-website/website"})))

(deftask make-war
         "Build a war for deployment"
         []
         (comp (hoplon)
               (cljs :optimizations :advanced)
               (uber :as-jars true)
               #_(web :serve 'ht.handler/app)
               (war)))

