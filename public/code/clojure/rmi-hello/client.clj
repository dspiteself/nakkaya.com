
(def rmi-registry (java.rmi.registry.LocateRegistry/getRegistry "127.0.0.1"))

(let [hello (.lookup rmi-registry "Hello")]
  (println (.sayHello hello)))
