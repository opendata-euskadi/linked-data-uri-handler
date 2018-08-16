How to test LOD in local weblogic
=================================

[1] - Enable the mod_jk proxy to tomcat
		a) ensure the tomcat mod_jk proxy is DISABLED for /r01hpLODWar
		
			# Send everything for context /r01hpLODWar to worker named [localhost_tomcat] (ajp13)
			# and defined at conf/pci/workers.properties
			JKMount  /r01hpLODWar/* localhost_tomcat
	
		b) ensure the weblogic proxy is DISABLED for /r01hpLODWar
		
			#<Location /r01hpLODWar>
			#	SetHandler weblogic-handler
			#	WebLogicHost localhost
			#	WebLogicPort 7001
			#</Location>

[2] - Add xercesImpl.jar and xml-apis.jar to the /r01hpLODWar/WebContent/WEB-INF/lib folder
      (there's a copy of these jars at  /r01hpLODWar/WebContent/WEB-INF/lib_tomcat

[3] - At r01hp.bootstrap.core.lod.R01HLODTripleStoreProxyServletGuiceModule,java COMMENT the welogic proxy section

[4] - At r01hp.lod.properties.xml ENABLE the generic proxy:


		<triplestore>
			<useGenericHttpProxy>true</useGenericHttpProxy>
			...
		</triplestore>
