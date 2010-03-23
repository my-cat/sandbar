; Copyright (c) Brenton Ashworth. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns sandbar.example.ideadb.app_development
  (:use [compojure.control :only (decorate)]
        (compojure.http [routes :only (defroutes GET ANY)]
                        [middleware :only (with-context)]
                        [helpers :only (serve-file)]
                        [servlet :only (servlet)]
                        [session :only (with-session)])
        [compojure.server.jetty :only (run-server)]
        (sandbar [library :only (page-not-found-404 app-context)]
                 security)
        (sandbar.example.ideadb
         [user_module :only (user-module-routes with-db-configured)]
         [admin_module :only (admin-module-routes)]
         [layouts :only (main-layout)])))

(defroutes development-routes
  user-module-routes
  admin-module-routes
  (GET "/public/*" (or (serve-file (params :*)) :next))
  (ANY "*" (main-layout "404" request (page-not-found-404))))

(def security-config
     [#"/admin.*"                   [:admin :ssl] 
      #"/idea/edit.*"               [:admin :ssl] 
      #"/idea/delete.*"             [:admin :ssl] 
      #"/idea/download.*"           :admin 
      #"/idea/permission-denied.*"  :any
      #"/idea/login.*"              [:any :ssl] 
      #".*.css|.*.js|.*.png|.*.gif" :any 
      #".*"                         [#{:admin :user} :nossl]])

(decorate development-routes
          (with-context @app-context)
          (with-db-configured)
          (with-security security-config "/idea")
          (with-secure-channel security-config 8080 8443)
          (with-session))

(run-server {:ssl true :port 8080 :ssl-port 8443
             :keystore "my.keystore"
             :key-password "foobar"}
            (str @app-context "/*")
            (servlet development-routes))

