How to test LOD in local weblogic
=================================

[1] - Enable the apache proxy to weblogic
		a) ensure the tomcat mod_jk proxy is DISABLED for /r01hpLODWar
		
			# Send everything for context /r01hpLODWar to worker named [localhost_tomcat] (ajp13)
			# and defined at conf/pci/workers.properties
			#JKMount  /r01hpLODWar/* localhost_tomcat
	
		b) ensure the weblogic proxy is ENABLED for /r01hpLODWar
		
			<Location /r01hpLODWar>
				SetHandler weblogic-handler
				WebLogicHost localhost
				WebLogicPort 7001
			</Location>

[2] - Remove xercesImpl.jar and xml-apis.jar from the /r01hpLODWar/WebContent/WEB-INF/lib dir

[3] - At r01hp.bootstrap.core.lod.R01HLODTripleStoreProxyServletGuiceModule,java UNCOMMENT the welogic proxy section

[4] - At r01hp.lod.properties.xml DISABLE the generic proxy:


		<triplestore>
			<useGenericHttpProxy>false</useGenericHttpProxy>
			...
		</triplestore>
