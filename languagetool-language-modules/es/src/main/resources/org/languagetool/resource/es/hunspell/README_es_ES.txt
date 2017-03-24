  ****************************************************************************
  **                                                                        **
  **          Diccionario para corrección ortográfica en español de         **
  **                      Apache OpenOffice/LibreOffice                     **
  **                                                                        **
  ****************************************************************************
  **  VERSIÓN GENÉRICA PARA TODAS LAS LOCALIZACIONES                        **
  ****************************************************************************

                                   Versión 2.1

SUMARIO

1. AUTOR
2. LICENCIA
3. INSTALACIÓN
4. COLABORACIÓN
5. AGRADECIMIENTOS


1. AUTOR

   Este diccionario ha sido desarrollado inicialmente por Santiago Bosio;
quien actualmente coordina el desarrollo de todos los diccionarios localizados.

   Si desea contactar al autor, por favor envíe sus mensajes mediante correo
electrónico a:

	santiago.bosio <en> gmail <punto> com
        (reemplace <en> por @ y <punto> por . al enviar su mensaje)

   El diccionario es un desarrollo completamente nuevo, y NO ESTÁ BASADO en el
trabajo de Jesús Carretero y Santiago Rodríguez, ni en la versión adaptada al
formato de MySpell por Richard Holt.


2. LICENCIA

   Este diccionario para corrección ortográfica, integrado por el fichero
de afijos y la lista de palabras (es_ANY[.aff|.dic]) se distribuye
bajo un triple esquema de licencias disjuntas: GNU GPL versión 3 o posterior,
GNU LGPL versión 3 o posterior, ó MPL versión 1.1 o posterior. Puede
seleccionar libremente bajo cuál de estas licencias utilizará este diccionario.
Encontrará copias de las licencias adjuntas en este mismo paquete.

3. INSTALACIÓN

   En Apache OpenOffice/LibreOffice, utilice el administrador de
extensiones, seleccionando para instalar directamente el fichero con
extensión ".oxt".

   Para instalar en OpenOffice.org versión 1.x ó 2.x, deberá realizar una
instalación manual siguiendo estas instrucciones:

a) Copie el fichero de afijos y la lista de palabras en la carpeta de
   instalación de diccionarios.

   Si tiene permisos de administrador, puede instalar el diccionario de
manera que esté disponible para todos los usuarios, copiando los ficheros al
directorio de diccionarios de la suite. Este directorio depende de la
plataforma de instalación. Podrá ubicarlo si ingresa en el ítem Opciones
del menú Herramientas. Despliegue la primera lista, etiquetada "OpenOffice.org"
y seleccione el ítem Rutas. La carpeta donde debe copiar los ficheros se
denomina "ooo", y la encontrará bajo el directorio que figura en la lista de
rutas con el tipo "Lingüística".

   En caso de no contar con permisos de administrador, igualmente puede
realizar una instalación para su usuario particular, copiando los ficheros al
directorio que figura en la lista de rutas con el tipo "Diccionarios definidos
por el usuario".

   Estos directorios de configuración usualmente están ocultos. Deberá ajustar
las opciones del administrador de ficheros que utiliza para que se muestren
este tipo de ficheros o directorios. Consulte la ayuda para su plataforma en
caso que no sepa cómo hacerlo.

b) Edite la lista de diccionarios disponibles para añadir el nuevo diccionario.

   En el directorio donde copió los diccionarios encontrará un fichero de
texto denominado "dictionary.lst". Modifíquelo como se indica a continuación,
utilizando el editor de textos de su preferencia.

   El formato de la lista permite definir tres tipos de diccionarios
diferentes: de corrección ortográfica (DICT), de sinónimos (THES) o de
separación silábica (HYPH).

   En este caso creará un nuevo ítem de tipo DICT. Para cada entrada de este
tipo, debe definir el lenguaje y la región (utilizando códigos ISO estándares),
y especificar el nombre base de los ficheros que definen el diccionario. Para
el español, el código ISO de lenguaje se escribe "es" (en minúsculas, sin las
comillas). El código de región depende de cómo tenga configurado su sistema
(por lo general será el del país donde reside), elegible entre uno de los
siguientes:

   Argentina:		"AR"		Honduras:		"HN"
   Bolivia:		"BO"		México:			"MX"
   Chile:		"CL"		Nicaragua:		"NI"
   Colombia:		"CO"		Panamá:			"PA"
   Costa Rica:		"CR"		Perú:			"PE"
   Cuba:		"CU"		Puerto Rico:		"PR"
   Rep. Dominicana:	"DO"		Paraguay:		"PY"
   Ecuador:		"EC"		El Salvador:		"SV"
   España:		"ES"		Uruguay:		"UY"
   Guatemala:		"GT"		Venezuela:		"VE"

   (El código de región se escribe en mayúsculas sin las comillas).

   El nombre base del fichero es igual al del fichero de diccionario o al de
afijos, sin la extensión (.dic o .aff).

   Por ejemplo, si ha descargado el paquete localizado para Argentina
(es_AR.zip), al descomprimirlo obtendrá los ficheros 'es_AR.dic' y
'es_AR.aff'. Después de copiarlos en el directorio correspondiente, la nueva
línea que deberá crear en el fichero 'dictionary.lst' es:

   DICT es AR es_AR

c) Reinicie OpenOffice.org.

   Guarde y cierre todos los documentos que tenga abiertos. Si utiliza la
plataforma de Microsoft Windows y tiene el inicio rápido de OpenOffice.org
activado, ciérrelo también.

   Inicie nuevamente alguna de las aplicaciones de OpenOffice.org (cualquiera
de ellas servirá).

d) Configure las opciones de lingüística del programa.

   Ingrese nuevamente al ítem Opciones del menú Herramientas y despliegue el
árbol "Configuración de idioma".

   Entre las opciones del ítem "Idiomas" hay una lista donde se configura el
idioma occidental utilizado como idioma predeterminado para los documentos
nuevos.

   Elija de esa lista el idioma y región que configuró en el fichero
'dictionary.lst'. Para el ejemplo utilizado sería "Español (Argentina)".

   Verifique que esta entrada de la lista aparezca con un pequeño tilde azul
y las letras ABC a su izquierda; esto indica que existe un diccionario de
corrección ortográfica instalado para esa localización. Si la marca no
aparece, debe haber cometido algún error en los pasos previos (el más común es
que haya dejado alguna ventana o el inicio rápido de OpenOffice.org
abiertos).

   Si necesitara ayuda para realizar cualquiera de estos pasos, envíe un
mensaje al encargado de mantenimiento del diccionario, o a las listas de
correo del proyecto Apache OpenOffice en español
(http://www.openoffice.org/es/soporte/listas.html).


4. COLABORACIÓN

   Este diccionario es resultado del trabajo colaborativo de muchas personas.
La buena noticia es que ¡usted también puede participar!

   ¿Tiene dudas o sugerencias? ¿Desearía ver palabras agregadas, o que se
realizaran correcciones? Sólo debe contactar al encargado de mantenimiento de
este diccionario, a través de su correo electrónico, quien se encargará de
evacuar sus dudas, o de realizar las modificaciones necesarias para la próxima
versión del diccionario.


5. AGRADECIMIENTOS

   Hay varias personas que han colaborado con aportes o sugerencias a la
creación de este diccionario. Se agradece especialmente a:

   - Richard Holt.
   - Marcelo Garrone.
   - Kevin Hendricks.
   - Juan Rey Saura.
   - Carlos Dávila.
   - Román Gelbort.
   - J. Eduardo Moreno.
   - Gonzalo Higuera Díaz.
   - Ricardo Palomares Martínez.
   - Sergio Medina.
   - Ismael Olea.
   - Alejandro Moreno.
   - Alexandro Colorado.
   - Andrés Sánchez.
   - Juan Rafael Fernández García.
   - eksperimental.
   - Ezequiel (ezeperez26).
   - KNTRO.
   - Ricardo Berlasso.
   - y a todos los integrantes de la comunidad en español que proponen mejoras
     a este diccionario.
