# Apache web server config:
====================================  

(see /r01fb/base/r01fbDocs/http_server/apache_install.txt)

## Rewrites
Pre-requisite:
```apache
	# Rewrite
	LoadModule rewrite_module modules/mod_rewrite.so
```

## Proxies:
Pre-requisite:  
[0] - Mod proxy

```apache
		# Proxy
		LoadModule proxy_module modules/mod_proxy.so
		LoadModule proxy_http_module  modules/mod_proxy_http.so
```
[1] - tomcat conector config
```apache
		# Tomcat connector module
		LoadModule  jk_module  modules/mod_jk.so
		JkWorkersFile conf/pci/workers.properties
		# JK Logging
		JkLogFile     logs/mod_jk.log
		# Set the jk log level [debug/error/info]
		JkLogLevel    info
		# Select the timestamp log format
		JkLogStampFormat "[%a %b %d %H:%M:%S %Y]"
```
[2] - workers.properties file:
```apache
		worker.list=localhost_tomcat

		# LOCALHOST: TOMCAT (ajp13)
		# -beaware that port is NOT the tomcat port... usually it's 8009-
		worker.localhost_tomcat.type=ajp13
		worker.localhost_tomcat.host=localhost
		worker.localhost_tomcat.port=8009
```

## Virtual Host:
```apache
<VirtualHost *:80>
    DocumentRoot "/home/linked_data"
    ServerName localhost
	 ServerAlias id.localhost
	 ServerAlias data.localhost
	 ServerAlias api.localhost
	 ServerAlias doc.localhost
    ErrorLog "logs/localhost-error.log"
    CustomLog "logs/localhost-access.log" common
	

	 RewriteEngine On
	 # elda static content
	 RewriteRule ^(/elda-assets/.*)$ $1 [last]
	 
	 # triple store console
	 RewriteRule ^/(read|write)/triplestore/(.*)$ /r01hpLODWar/$1/triplestore/$2 [proxy,last]
	 
	 # any other resource
	 RewriteRule ^/((?!r01hpLODWar/).+)$ /r01hpLODWar/$1 [proxy,last,nosubreq]
	 
	 #LogLevel alert rewrite:trace3
	
	 # Proxies
	 # =============================================================================
	 # Send everything for context /examples to worker named [localhost_tomcat] (ajp13)
	 # and defined at conf/pci/workers.properties
	 JKMount  /r01hpLODWar/* localhost_tomcat
</VirtualHost>


Alias /elda-assets /home/linked_data/r01hpLODWebContent/elda-assets
<Directory "/home/linked_data/r01hpLODWebContent/">
    Options Indexes FollowSymLinks MultiViews
    AllowOverride None
    Require all granted
</Directory>
```