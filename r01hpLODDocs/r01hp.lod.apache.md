# Apache web server config:
====================================  

(see /r01fb/base/r01fbDocs/http_server/apache_install.txt)

## Rewrites
Pre-requisite:
```apache
	# Rewrite
	LoadModule rewrite_module modules/mod_rewrite.so
```
Rewrite rules:
```apache		
	RewriteEngine On
	RewriteRule ^/(id|eli|dataset|distribution|doc|data|def|sparql)/?(.*) /r01hpLODWar/$1/$2 [proxy]
	RewriteRule ^/(read|write)/triplestore/(.*) /r01hpLODWar/$1/blazegraph/$2 [proxy]
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
Proxy:
```apache
    JKMount  /r01hpProxyWar/* localhost_tomcat
```
