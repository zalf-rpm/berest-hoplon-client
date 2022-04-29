(set-env!
  :dependencies '[[adzerk/boot-cljs "2.1.5"]
                  [adzerk/boot-reload "0.6.1"]
                  [compojure "1.4.0"]
                  #_[hoplon/boot-hoplon "0.1.13"]
                  [hoplon/boot-hoplon "0.3.0"]
                  [hoplon/castra "3.0.0-alpha7"]
                  [hoplon/hoplon "7.2.0"]
                  [org.clojure/clojure "1.10.3"]
                  [org.clojure/clojurescript "1.10.758"]
                  [pandeiro/boot-http "0.8.3"]
                  [ring "1.4.0"]
                  [ring/ring-defaults "0.1.5"]
                  [com.cognitect/transit-clj "1.0.329"]
                  #_[hoplon/twitter-bootstrap "0.2.0"]
                  [cljsjs/bootstrap "3.3.6-1"]
                  #_[cljsjs/highcharts "4.2.5-1" #_"4.1.10-2"]
                  [cljsjs/highcharts "9.3.2-0"]
                  #_[cljsjs/jquery "1.11.3-0"]
                  [cljsjs/jquery "3.4.0-0"]
                  [cljs-ajax "0.8.4"]
                  [com.andrewmcveigh/cljs-time "0.4.0"]
                  [javax.xml.bind/jaxb-api "2.3.0"]]
  ;:out-path      
  ;:resource-paths #{"assets" "src/clj"}
  :source-paths   #{"src/cljs"
                    "src/hl"
                    "../twitter-bootstrap/src"})

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
           #_(speak)
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
           (cljs :optimizations :simple #_:advanced)
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

