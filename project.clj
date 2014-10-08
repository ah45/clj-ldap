(defproject org.clojars.pntblnk/clj-ldap "0.0.9"
  :description "Clojure ldap client (development fork of alienscience's clj-ldap)."
  :url "https://github.com/pauldorman/clj-ldap"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.unboundid/unboundid-ldapsdk "3.1.0"]]
  :profiles {:dev {:dependencies
                   [[org.apache.directory.server/apacheds-all "1.5.5"]
                    [fs "1.3.3"]
                    [org.slf4j/slf4j-simple "1.5.6"]]}}
  :aot [clj-ldap.client]
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"})
