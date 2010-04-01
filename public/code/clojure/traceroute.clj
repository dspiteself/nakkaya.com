(ns traceroute
  (:import (java.net InetAddress)
	   (java.net URL)
	   (java.util Arrays)
	   (java.io BufferedReader InputStreamReader)
	   (jpcap NetworkInterface JpcapCaptor JpcapSender PacketReceiver)
	   (jpcap.packet EthernetPacket IPPacket ICMPPacket)))

(defn mac-byte-to-string [mac-bytes]
  (let [v  (apply vector 
		  (map #(Integer/toHexString (bit-and % 0xff)) mac-bytes))]
    (apply str (interpose ":" v))))

(defn ipv4? [inet-addrs]
  (if (instance? java.net.Inet4Address inet-addrs)
    true false))

(defn ipv4-addrs [addr-list]
  (filter ipv4? (apply vector (map (fn[i] (.address i)) addr-list))))

(defn device [name]
  (first (filter #(= name (.name %)) (JpcapCaptor/getDeviceList))))

(defn captor [device]
  (JpcapCaptor/openDevice device 2000 false 5000))

(defn gateway-mac [captor device]
  (let [ping-addr (InetAddress/getByName "google.com")
	conn #(-> (URL. "http://google.com") .openStream .close)] 
    (conn)
    (loop [packet (.getPacket captor)]
      (if (Arrays/equals (-> packet .datalink .dst_mac)(.mac_address device))
	(-> packet .datalink .src_mac)
	(do (conn) 
	    (recur (.getPacket captor)))))))

(defn icmp-packet [device gw-mac this-ip target-ip]
  (let [icmp (ICMPPacket.)
	ether (EthernetPacket.)
	this-ip (cast java.net.InetAddress this-ip)
	target-ip (cast java.net.InetAddress target-ip)]
    (set! (.type icmp) ICMPPacket/ICMP_ECHO)
    (set! (.seq icmp) 100)
    (set! (.id icmp) 0)
    (.setIPv4Parameter icmp
    		       0 false false false 0 false false false 0 0 
    		       0 IPPacket/IPPROTO_ICMP this-ip target-ip)
    (set! (.data icmp) (.getBytes "data"))
    ;;ether
    (set! (.frametype ether) EthernetPacket/ETHERTYPE_IP)
    (set! (.src_mac ether) (.mac_address device))
    (set! (.dst_mac ether) gw-mac)
    ;;link
    (set! (.datalink icmp) ether)
    icmp))

(defn inc-hop [icmp]
  (set! (.hop_limit icmp) (inc (.hop_limit icmp))))

(defn type? [packet expected]
  (= (.type packet) expected))

(defn send-icmp-batch [sender icmp]
  (doseq [i (range 3)] 
    (.sendPacket sender icmp)))

(defn add [route icmp packet]
  (let [host (.getHostAddress (.src_ip packet))]
    (if-not (route host)
      (assoc route host (.hop_limit icmp)) route)))

(defn traverse [device gw-mac captor dev-ip target-ip]
  (let [icmp (icmp-packet device gw-mac dev-ip target-ip)
	sender (.getJpcapSenderInstance captor)] 
    (.setFilter captor 
    		(str "icmp and dst host " (.getHostAddress dev-ip)) true)
    (send-icmp-batch sender icmp)
    (loop [packet (.getPacket captor)
	   route {}]
      (cond 
       (nil? packet) 
       (do (send-icmp-batch sender icmp)
	   (recur (.getPacket captor) route))
       ;;
       (type? packet ICMPPacket/ICMP_TIMXCEED)
       (do (inc-hop icmp)
	   (send-icmp-batch sender icmp)
	   (recur (.getPacket captor) (add route icmp packet)))
       ;;
       (type? packet ICMPPacket/ICMP_UNREACH) (add route icmp packet)
       (type? packet ICMPPacket/ICMP_ECHOREPLY) (add route icmp packet)))))

(defn hostip-info [ip]
  (let [url (URL. (str "http://api.hostip.info/get_html.php?ip=" ip))]
    (with-open [stream (. url (openStream))]
	       (let [buf (BufferedReader. (InputStreamReader. stream))]
		 (vec (line-seq buf))))))

(defn map-ip-data [route]
  (map deref (doall (map #(future (hostip-info (first %))) route))))

(defn traceroute [device-name target-ip]
  (let [dev (device device-name)
	dev-ip (first (ipv4-addrs (.addresses dev)))
	cap (captor dev)
	gw-mac (gateway-mac cap dev)
	target-ip (InetAddress/getByName target-ip)
	route (map-ip-data 
	       (sort-by second (traverse dev gw-mac cap dev-ip target-ip)))]
    (doseq [[country city ip] route] 
      (println ip "\n\t" country "\n\t" city))))

(traceroute "en1" "google.com")
