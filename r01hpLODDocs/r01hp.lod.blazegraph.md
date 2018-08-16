
# Blazegraph install
====================================  
Doc: https://www.blazegraph.com/

1.- Download blazegraph from https://www.blazegraph.com/  
2.- Copy .jar in server folder (eg. /servers/lod/blazegraph)  
3.- Start BlazeGraph in eclipse. See "lod.eclipse.read.me" file.  
4.- http://localhost:9999/blazegraph in browser.  

# BlazeGraph in eclipse
====================================

1.- Goto `[Run] > [External Tools] > [External Tools Configuration...]`

2.- Create a NEW config with:

    |--------------------|---------------------|-----------------------------------------------------------|
    | Location:          | jdk location        | eg. "/develop/java/jdk1.8.0_121/bin/java"                 |
    | Working Directory: | blazegraph location | eg. "/servers/lod/blazegraph"                             |
    | Arguments:         | blazegraph jar      | eg. "-server -jar /servers/lod/blazegraph/blazegraph.jar" |

    
 # BlazeGraph data sync
============================================

Pasos para actualizar el BG de escritura con el de lectura:

	Entorno: PRODUCCION

		1º Conectarnos al servidor:
 
			Server: ejlp1508ges (BG Escritura ACTIVO)
			User: lod
			Password: temp0ral

		2º Ejecutar los siguientes comandos:

			su spproy
			sudo rootsh
	
		3º Ejecutar donde estas los siguientes scripts en este orden, para así actualizar los BG de lectura con el de escritura:

			/aplic/lod/triplestore/scripts/backup_store.sh -----> Pedira la password del usuario lod que es temp0ral
				Este script: 
					- Borra el fichero blazegraph.jnl del directorio backup y lo actualiza con el actual del BG de escritura activo.
					- Manda el fichero actual blazegraph.jnl que se ha dejado en la zona de backup al otro servidor que es el BG de escritua pasivo.
				
			/aplic/lod/triplestore/scripts/actualizar_query_11.sh
			/aplic/lod/triplestore/scripts/actualizar_query_12.sh
				Estos scripts:
					- Paran el tomcat correspondiente a cada uno de los BG de lectura.
					- Copian el fichero actual blazegraph.jnl que se ha dejado en la zona de backup a cada BG de lectura del propio servidor.
					- Inician el tomcat parado anteriormente en cada caso.

		4º Conectarnos al servidor:
 
			Server: ejlp1509ges (BG Escritura PASIVO)
			User: lod
			Password: temp0ral

		5º Ejecutar los siguientes comandos:

			su spproy
			sudo rootsh
	
		6º Ejecutar donde estas los siguientes scripts en este orden:

			/aplic/lod/triplestore/scripts/actualizar_store_2.sh
				Este script:
					- Para el tomcat correspondiente al BG de escritura pasivo.
					- Copia el fichero blazegraph.jnl dejado en la zona de backup por el primer script del punto 3 a la zona correspondiente.
					- Inicia el tomcat parado anteriormente.
					
			/aplic/lod/triplestore/scripts/actualizar_query_21.sh
			/aplic/lod/triplestore/scripts/actualizar_query_22.sh
				Estos scripts:
					- Paran el tomcat correspondiente a cada uno de los BG de lectura.
					- Copian el fichero actual blazegraph.jnl que se ha dejado en la zona de backup a cada BG de lectura del propio servidor.
					- Inician el tomcat parado anteriormente en cada caso.


	Entorno: DESARROLLO

		1º Conectarnos al servidor:
 
			Server: ejld1255 (BG Escritura ACTIVO)
			User: lod
			Password: temp0ral

		2º Ejecutar los siguientes comandos:

			su spproy
			sudo rootsh
	
		3º Ejecutar donde estas los siguientes scripts en este orden, para así actualizar los BG de lectura con el de escritura:

			/aplic/lod/triplestore/scripts/backup_store.sh -----> Pedira la password del usuario lod que es temp0ral
				Este script: 
					- Borra el fichero blazegraph.jnl del directorio backup y lo actualiza con el actual del BG de escritura activo.
					- Manda el fichero actual blazegraph.jnl que se ha dejado en la zona de backup al otro servidor que es el BG de escritua pasivo.
				
			/aplic/lod/triplestore/scripts/actualizar_query_11.sh
			/aplic/lod/triplestore/scripts/actualizar_query_12.sh
				Estos scripts:
					- Paran el tomcat correspondiente a cada uno de los BG de lectura.
					- Copian el fichero actual blazegraph.jnl que se ha dejado en la zona de backup a cada BG de lectura del propio servidor.
					- Inician el tomcat parado anteriormente en cada caso.

		4º Conectarnos al servidor:
 
			Server: ejld1256 (BG Escritura PASIVO)
			User: lod
			Password: temp0ral

		5º Ejecutar los siguientes comandos:

			su spproy
			sudo rootsh
	
		6º Ejecutar donde estas los siguientes scripts en este orden:

			/aplic/lod/triplestore/scripts/actualizar_store_2.sh
				Este script:
					- Para el tomcat correspondiente al BG de escritura pasivo.
					- Copia el fichero blazegraph.jnl dejado en la zona de backup por el primer script del punto 3 a la zona correspondiente.
					- Inicia el tomcat parado anteriormente.
					
			/aplic/lod/triplestore/scripts/actualizar_query_21.sh
			/aplic/lod/triplestore/scripts/actualizar_query_22.sh
				Estos scripts:
					- Paran el tomcat correspondiente a cada uno de los BG de lectura.
					- Copian el fichero actual blazegraph.jnl que se ha dejado en la zona de backup a cada BG de lectura del propio servidor.
					- Inician el tomcat parado anteriormente en cada caso.
					

