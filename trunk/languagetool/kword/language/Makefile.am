
INCLUDES = $(all_includes)
liblanguagetool_la_LIBADD = $(LIB_KSYCOCA)
#-llangtool

####### Files

kde_module_LTLIBRARIES = liblanguagetool.la

liblanguagetool_la_SOURCES = main.cc ConfigDialog.cc

liblanguagetool_la_LDFLAGS = $(all_libraries) -avoid-version -module -no-undefined

noinst_HEADERS = main.h ConfigDialog.h

liblanguagetool_la_METASOURCES = main.moc ConfigDialog.moc

service_DATA = languagetool.desktop
servicedir = $(kde_servicesdir)

# fixme: false friends XML:
#datadir = $(kde_datadir)/language
#data_DATA = false_friends.xml
