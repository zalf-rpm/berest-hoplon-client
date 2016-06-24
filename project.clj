(defproject de.zalf.berest/berest-hoplon-client "1.0.0-SNAPSHOT"
  :dependencies [[adzerk/boot-cljs "1.7.170-3"]
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
                 [cljsjs/bootstrap "3.3.6-1"]
                 [cljsjs/highcharts "4.2.5-1" #_"4.1.10-2"]
                 [cljsjs/jquery "1.11.3-0"]

                 [cljs-ajax "0.2.3"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]]
  :source-paths ["src"])
