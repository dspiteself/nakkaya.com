(ns app.run
  (:use compojure)
  (:use :reload-all app.routes))

(run-server {:port 8085} "/*" (servlet web-app))
