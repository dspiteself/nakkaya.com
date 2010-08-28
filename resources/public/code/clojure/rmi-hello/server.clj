
(def rmi-registry (java.rmi.registry.LocateRegistry/createRegistry 1099))

(defn stop-rmi []
  (java.rmi.server.UnicastRemoteObject/unexportObject rmi-registry true))


(defn hello-server []
  (proxy [stub.Hello] [] 
    (sayHello [] "Hello, World!") ))

(defn register-server []
  (.bind
   (java.rmi.registry.LocateRegistry/getRegistry)
   "Hello"
   (java.rmi.server.UnicastRemoteObject/exportObject (hello-server) 0)))


(register-server)
(while true (Thread/sleep 1000))
