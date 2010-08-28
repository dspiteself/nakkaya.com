(ns pcap
  (:use :reload-all clojure.contrib.command-line)
  (:import (java.net InetAddress)
	   (jpcap NetworkInterface JpcapCaptor JpcapSender PacketReceiver)
	   (jpcap.packet EthernetPacket ARPPacket)))

(defn mac-byte-to-string [mac-bytes]
  (let [v  (apply vector 
		  (map #(Integer/toHexString (bit-and % 0xff)) mac-bytes))]
    (apply str (interpose ":" v))))

(defn print-device-info [name mac]
  (format "%1$-4s %2$-17s" name mac))

(defn ipv4? [inet-addrs]
  (if (instance? java.net.Inet4Address inet-addrs)
    true false))

(defn ipv4-addrs [addr-list]
  (filter ipv4? (apply vector (map (fn[i] (.address i)) addr-list))))

(defn interface-index [name]
  (loop [index 0
	 device      (first (JpcapCaptor/getDeviceList))
	 device-list (rest (JpcapCaptor/getDeviceList))]
    (if (= name (.name device))
      index
      (recur (inc index) (first device-list) (rest device-list)))))

(defn interface-by-name [name]
  (nth (JpcapCaptor/getDeviceList) (interface-index name)))

(defn interface-ip [interface]
  (first (ipv4-addrs (.addresses interface))))

(defn interface-info []
  (doseq [device  (JpcapCaptor/getDeviceList)]
    (let  [name   (.name device)
	   mac    (mac-byte-to-string (.mac_address device))
	   ip     (interface-ip device)]
      (println (print-device-info name mac) ip))))

(defn generateip-ip-list [interface]
  (let [ip (.getHostAddress (interface-ip interface))
	block (.substring ip 0 (+ 1 (.lastIndexOf ip ".")))]
    (vec (map #(str block %) (range 1 255)))))

(defn create-arp-request [interface target]
  (let  [broadcast    (into-array (Byte/TYPE) (repeat 6 (byte 255)))
	 srcip        (interface-ip interface)
	 arp-packet   (ARPPacket.)
	 ether-packet (EthernetPacket.)]
    ;;arp
    (set! (.hardtype arp-packet) ARPPacket/HARDTYPE_ETHER)
    (set! (.prototype arp-packet) ARPPacket/PROTOTYPE_IP)
    (set! (.operation arp-packet) ARPPacket/ARP_REQUEST)
    (set! (.hlen arp-packet) 6)
    (set! (.plen arp-packet) 4)
    (set! (.sender_hardaddr arp-packet) (.mac_address interface))
    (set! (.sender_protoaddr arp-packet) (.getAddress srcip))
    (set! (.target_hardaddr arp-packet) broadcast)
    (set! (.target_protoaddr arp-packet) 
	  (.getAddress (InetAddress/getByName target)))
    ;;ether
    (set! (.frametype ether-packet) EthernetPacket/ETHERTYPE_ARP)
    (set! (.src_mac ether-packet) (.mac_address interface))
    (set! (.dst_mac ether-packet) broadcast)
    ;;wire
    (set! (.datalink arp-packet) ether-packet)
    arp-packet))

(defn open-captor [interface]
  (JpcapCaptor/openDevice interface 50 true 0))

(defn send-arp-probe [captor interface ip-list]
  (let [sender (.getJpcapSenderInstance captor)] 
    (doseq[ip ip-list]
      (.sendPacket sender (create-arp-request interface ip)))))

(defn packet-callback []
  (proxy [PacketReceiver] []
    (receivePacket
     [packet]
     (if (instance? ARPPacket packet)
       (let  [src-ip (.getSenderProtocolAddress packet)
	      src-mac (.getSenderHardwareAddress packet)] 
	 (println (.getHostAddress src-ip) " is at " src-mac))))))

(defn arp-sweep [interface]
  (let  [interface (interface-by-name interface)
	 captor (open-captor interface)]
    
    (send-arp-probe captor interface (generateip-ip-list interface))

    (doto (Thread. #(.loopPacket captor -1 (packet-callback)))
      (.start))
    
    (Thread/sleep 3000)
    (.breakLoop captor)))

(with-command-line *command-line-args*
  "clojure pcap"
  [[list? "List interfaces."]
   [device "Device to use" "en1"]]
  (cond
   list? (interface-info)
   device (arp-sweep device)
   :else (println "Invalid option")))
