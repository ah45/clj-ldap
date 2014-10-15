(ns clj-ldap.test.server
  "An embedded ldap server for unit testing"
  (:require [clojure.java.io :as io]
            [clj-ldap.client :as ldap])
  (:import [com.unboundid.util.ssl
            KeyStoreKeyManager
            TrustAllTrustManager
            SSLUtil])
  (:import [com.unboundid.ldap.listener
            InMemoryDirectoryServer
            InMemoryDirectoryServerConfig
            InMemoryListenerConfig]))

(defonce server (atom nil))

(def base-dn "dc=alienscience,dc=org,dc=uk")

(def default-key-store "clj-ldap.ks")
(def default-key-store-pin (char-array "clj-ldap"))

(defn ssl-socket-factory
  []
  (let [ks (KeyStoreKeyManager. (-> default-key-store io/resource io/file)
                                default-key-store-pin)
        tm (TrustAllTrustManager.)
        ssl (SSLUtil. ks tm)]
    (.createSSLServerSocketFactory ssl)))

(defn generate-listener
  [port]
  (InMemoryListenerConfig/createLDAPConfig "ldap" port))

(defn generate-ssl-listener
  [port]
  (InMemoryListenerConfig/createLDAPSConfig "ldaps" port (ssl-socket-factory)))

(defn generate-config
  [port ssl-port]
  (doto (InMemoryDirectoryServerConfig. (into-array String [base-dn]))
    (.setListenerConfigs [(generate-listener port)
                          (generate-ssl-listener ssl-port)])))

(defn start-ldap-server
  "Start an embedded ldap server instance"
  [config]
  (doto (InMemoryDirectoryServer. config)
    (.startListening)))

(defn add-toplevel-objects!
  "Adds top level objects, needed for testing, to the ldap server"
  [connection]
  (ldap/add connection "dc=alienscience,dc=org,dc=uk"
            {:objectClass ["top" "domain" "extensibleObject"]
             :dc "alienscience"})
  (ldap/add connection "ou=people,dc=alienscience,dc=org,dc=uk"
            {:objectClass ["top" "organizationalUnit"]
             :ou "people"})
  (ldap/add connection
            "cn=Saul Hazledine,ou=people,dc=alienscience,dc=org,dc=uk"
            {:objectClass ["top" "Person"]
             :cn "Saul Hazledine"
             :sn "Hazledine"
             :description "Creator of bugs"}))

(defn stop!
  "Stops the embedded ldap server"
  []
  (if @server
    (.shutDown @server true)
    (reset! server nil)))

(defn start!
  "Starts an embedded ldap server on the given port"
  ([port ssl-port]
   (start! (generate-config port ssl-port)))
  ([config]
   (stop!)
   (reset! server (start-ldap-server config))
   (let [p (.getListenPort @server "ldap")
         c (ldap/connect {:host {:address "localhost" :port p}})]
     (add-toplevel-objects! c))))
