/*
   $Id: ConfigDialog.cc,v 1.3 2003-08-26 01:36:43 dnaber Exp $
   This file is part of the KDE project
   Copyright (C) 2003 Daniel Naber <daniel.naber@t-online.de>
   This is a frontend to a simple 'Style and Grammar Checker': it offers a simple 
   grammar check and warns if frequently misused words or 'false friends' occur in 
   the text.
*/
/***************************************************************************
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 ***************************************************************************/

/* TODO: make it possible to select options with a click on
   their name, not only directly in the check box (thus make
   it possible to select many options by holding down the mouse
   button and scrollig down) -> see "fixme" in the code
*/

#include <stdlib.h>

#include <qapplication.h>
#include <qfile.h>
#include <qlabel.h>
#include <qlayout.h>
#include <qpushbutton.h>
#include <qspinbox.h>
#include <qstringlist.h>
#include <qvbox.h>
#include <qhbox.h>

#include <kapplication.h>
#include <kcombobox.h>
#include <kconfig.h>
#include <kcursor.h>
#include <kdatatool.h>
#include <kdebug.h>
#include <klocale.h>
#include <kmessagebox.h>
#include <kstandarddirs.h>

#include "ConfigDialog.h"

/***************************************************
 *
 * Configuration dialog *
 ***************************************************/

ConfigDialog::ConfigDialog() : 
	KDialogBase(KJanusWidget::Tabbed, i18n("Style and Grammar Help Configuration"),
		KDialogBase::Help|KDialogBase::Ok|KDialogBase::Cancel, KDialogBase::Ok)
{
	
	kdDebug() << "ConfigDialog()" << endl;
	setHelp(QString::null, "languagetool");	// TODO: more exact link?
	resize(500, 400);

	//
	// load configuration
	//
	kdDebug() << "loading configuration" << endl;
	KConfig general("languagetool");
	general.setGroup("General");

	// load configuration: grammar:
	QStrList grammar_rules;		// these are just IDs, so QStringList is not necessary
	bool has_grammar_rules = false;		// used so that all default to on the first time
	if( general.hasKey("GrammarRules") ) {
		general.readListEntry("GrammarRules", grammar_rules);
		has_grammar_rules = true;
	}

	// false friends:
	QString mother_tongue = general.readEntry("MotherTongue", "en");
	QString text_language = general.readEntry("TextLanguage", "en");
	QStrList false_friends_rules;
	bool has_false_friends_rules = false;
	if( general.hasKey("FalseFriendsRules") ) {
		general.readListEntry("FalseFriendsRules", false_friends_rules);
		has_false_friends_rules = true;
	}
	
	// words:
	QStrList words_rules;
	bool has_words_rules = false;
	if( general.hasKey("WordsRules") ) {
		general.readListEntry("WordsRules", words_rules);
		has_words_rules = true;
	}

	//
	// Page 1
	//
	kdDebug() << "setting up page 1" << endl;
	QFrame *page = addPage(i18n("&Spelling and Grammar"));
	QVBoxLayout *topLayout = new QVBoxLayout(page, KDialog::marginHint(), KDialog::spacingHint());
	listview1 = new QListView(page);
	listview1->addColumn( i18n("Warning messages") );
	listview1->setColumnWidthMode(0, QListView::Maximum);
	listview1->setResizeMode(QListView::LastColumn);
	connect(listview1, SIGNAL(clicked(QListViewItem *)), 
		this, SLOT(slotItemClicked(QListViewItem *)));
	// TODO: enable again, so example sentences for this rule can be shown in a 
	// text view below:
	listview1->setSelectionMode(QListView::NoSelection);
	QDict<QString> items = getGrammarItems();
	QDictIterator<QString> it(items);
	for( ; it.current(); ++it ) {
		QString name = it.currentKey();
		QString *id = it.current();
		QCheckListItem *item = new QCheckListItem(listview1, name, QCheckListItem::CheckBox);
		if( grammar_rules.find(id->latin1()) != -1 || ! has_grammar_rules ) {
			item->setOn(true);
		}
		// key = ID of rule, value = QCheckListItem (we need this so we can
		// save the user's settings):
		m_grammar_checkboxes.insert(*id, item);
	}
	check1 = new QCheckBox(i18n("&Enable these rules"), page);
	connect(check1, SIGNAL(toggled(bool)), 
		this, SLOT(slotToggleGrammar(bool)));
	topLayout->addWidget(check1);
	topLayout->addWidget(listview1);
	
	//
	// Page 2
	//
	kdDebug() << "setting up page 2" << endl;
	QFrame *page2 = addPage(i18n("&False Friends"));
	QVBoxLayout *topLayout2 = new QVBoxLayout(page2, KDialog::marginHint(), KDialog::spacingHint());
	listview2 = new QListView(page2);
	listview2->addColumn( i18n("Word") );
	listview2->addColumn( i18n("Similar sounding word") );
	listview2->setAllColumnsShowFocus(true);
	listview2->setColumnWidthMode(0, QListView::Maximum);
	listview2->setResizeMode(QListView::LastColumn);
	connect(listview2, SIGNAL(clicked(QListViewItem *)), 
		this, SLOT(slotItemClicked(QListViewItem *)));
	// TODO: enable again, so example sentences for this rule can be shown in a 
	// text view below:
	listview2->setSelectionMode(QListView::NoSelection);
	QDict<QString> items2 = getFalseFriendsItems(listview2, false_friends_rules, 
		m_false_friends_checkboxes, has_false_friends_rules);
	listview2->setShowSortIndicator (true);
	listview2->sort();		// FIXME: case-insensitive

	check2 = new QCheckBox(i18n("&Enable these rules"), page2);
	connect(check2, SIGNAL(toggled(bool)), 
		this, SLOT(slotToggleFalseFriends(bool)));
	topLayout2->addWidget(check2);

	QHBox *hbox = new QHBox(page2);
	QLabel *label2a = new QLabel("Your &mother tongue:", hbox);
	combo_mother_tongue = new QComboBox(hbox);
	label2a->setBuddy(combo_mother_tongue);
	QHBox *hbox2 = new QHBox(page2);
	QLabel *label2b = new QLabel("&Text language:", hbox2);
	combo_text_language = new QComboBox(hbox2);
	label2b->setBuddy(combo_text_language);
	QStringList languages;
	languages += "en";
	languages += "de";
	languages += "fr";
	combo_mother_tongue->insertStringList(languages);
	combo_text_language->insertStringList(languages);
	topLayout2->addWidget(hbox);
	topLayout2->addWidget(hbox2);

	topLayout2->addWidget(listview2);

	//
	// Page 3
	//
	kdDebug() << "setting up page 3" << endl;
	QFrame *page3 = addPage(i18n("&Words"));
	QVBoxLayout *topLayout3 = new QVBoxLayout(page3, KDialog::marginHint(), KDialog::spacingHint());
	listview3 = new QListView(page3);
	listview3->addColumn( i18n("Show warnings for these words") );
	listview3->setColumnWidthMode(0, QListView::Maximum);
	listview3->setResizeMode(QListView::LastColumn);
	connect(listview3, SIGNAL(clicked(QListViewItem *)), 
		this, SLOT(slotItemClicked(QListViewItem *)));
	// TODO: enable again, so example sentences for this rule can be shown in a 
	// text view below:
	listview3->setSelectionMode(QListView::NoSelection);
	QDict<QString> items3 = getWordsItems();
	QDictIterator<QString> it3(items3);
	for( ; it3.current(); ++it3 ) {
		QString name = it3.currentKey();
		QString *id = it3.current();
		QCheckListItem *item = new QCheckListItem(listview3, name, QCheckListItem::CheckBox);
		if( words_rules.find(id->latin1()) != -1 || ! has_words_rules ) {
			item->setOn(true);
		}
		m_words_checkboxes.insert(*id, item);
	}
	check3 = new QCheckBox(i18n("&Enable these rules"), page3);
	connect(check3, SIGNAL(toggled(bool)), 
		this, SLOT(slotToggleWords(bool)));
	topLayout3->addWidget(check3);
	topLayout3->addWidget(listview3);

	//
	// Page 4
	//
	kdDebug() << "setting up page 4" << endl;
	QFrame *page4 = addPage(i18n("&Misc"));
	QVBoxLayout *topLayout4 = new QVBoxLayout(page4, KDialog::marginHint(), KDialog::spacingHint());

	check_sentence_length = new QCheckBox(i18n("&Recommended maximum sentence length:"), page4);
	connect(check_sentence_length, SIGNAL(toggled(bool)), 
		this, SLOT(slotToggleSentenceLength(bool)));
	sentence_spinbox = new QSpinBox(0, 1000, 1, page4);
	sentence_spinbox->setSuffix(i18n(" words"));

	check_whitespace = new QCheckBox(i18n("Check &Whitespace"), page4);
	connect(check_whitespace, SIGNAL(toggled(bool)), 
		this, SLOT(slotToggleWhitespaceCheck(bool)));

	check_articles = new QCheckBox(i18n("Check 'a' vs. 'an' &articles"), page4);
	connect(check_articles, SIGNAL(toggled(bool)), 
		this, SLOT(slotToggleArticleCheck(bool)));

	topLayout4->addWidget(check_sentence_length);
	topLayout4->addWidget(sentence_spinbox);
	topLayout4->addWidget(check_whitespace);
	topLayout4->addWidget(check_articles);
	topLayout4->addStretch();

	//
	// activate configuration
	//
	slotToggleGrammar(general.readBoolEntry("EnableGrammar", true));
	slotToggleFalseFriends(general.readBoolEntry("EnableFalseFriends", true));
	slotToggleWords(general.readBoolEntry("EnableWords", true));
	// TODO: don't insert, but select (so order is always the same):
	combo_mother_tongue->insertItem(mother_tongue, 0);
	combo_text_language->insertItem(text_language, 0);
	// Misc page:
	slotToggleSentenceLength(general.readBoolEntry("EnableSentenceLength", false));
	sentence_spinbox->setValue(general.readNumEntry("MaxSentenceLength", 30));
	slotToggleWhitespaceCheck(general.readBoolEntry("EnableWhitespaceCheck", true));
	slotToggleArticleCheck(general.readBoolEntry("EnableArticleCheck", true));
		
}


ConfigDialog::~ConfigDialog()
{
	if( m_dialog ) {
		delete m_dialog;
	}
}

void ConfigDialog::saveConfig()
{
	kdDebug() << "loading configuration" << endl;
	KConfig general("languagetool");
	general.setGroup("General");

	general.writeEntry("EnableGrammar", check1->isOn());
	general.writeEntry("EnableFalseFriends", check2->isOn());
	general.writeEntry("EnableWords", check3->isOn());
	general.writeEntry("EnableSentenceLength", check_sentence_length->isOn());
	general.writeEntry("EnableWhitespaceCheck", check_whitespace->isOn());
	general.writeEntry("EnableArticleCheck", check_articles->isOn());
	general.writeEntry("MaxSentenceLength", sentence_spinbox->value());

	general.writeEntry("MotherTongue", combo_mother_tongue->currentText());
	general.writeEntry("TextLanguage", combo_text_language->currentText());

	QStrList config_rules;
	// grammar:
	QDictIterator<QCheckListItem> it(m_grammar_checkboxes);
	for( ; it.current(); ++it ) {
		QString key = it.currentKey();
		QCheckListItem *box = it.current();
		if( box->isOn() ) {
			config_rules.append(key.latin1());
		}
	}
	general.writeEntry("GrammarRules", config_rules);
	config_rules.clear();

	// false friends:
	QDictIterator<QCheckListItem> it2(m_false_friends_checkboxes);
	for( ; it2.current(); ++it2 ) {
		QString key = it2.currentKey();
		QCheckListItem *box = it2.current();
		if( box->isOn() ) {
			config_rules.append(key.latin1());
		}
	}
	general.writeEntry("FalseFriendsRules", config_rules);
	config_rules.clear();

	// words:
	QDictIterator<QCheckListItem> it3(m_words_checkboxes);
	for( ; it3.current(); ++it3 ) {
		QString key = it3.currentKey();
		QCheckListItem *box = it3.current();
		if( box->isOn() ) {
			config_rules.append(key.latin1());
		}
	}
	general.writeEntry("WordsRules", config_rules);
	config_rules.clear();

	general.sync();
}

void ConfigDialog::slotToggleGrammar(bool enable)
{
	check1->setChecked(enable);
	listview1->setEnabled(enable);
}

void ConfigDialog::slotToggleFalseFriends(bool enable)
{
	check2->setChecked(enable);
	listview2->setEnabled(enable);
	combo_text_language->setEnabled(enable);
	combo_mother_tongue->setEnabled(enable);
}

void ConfigDialog::slotToggleWords(bool enable)
{
	check3->setChecked(enable);
	listview3->setEnabled(enable);
}

void ConfigDialog::slotToggleSentenceLength(bool enable)
{
	check_sentence_length->setChecked(enable);
	sentence_spinbox->setEnabled(enable);
}

void ConfigDialog::slotToggleWhitespaceCheck(bool enable)
{
	check_whitespace->setChecked(enable);
}

void ConfigDialog::slotToggleArticleCheck(bool enable)
{
	check_articles->setChecked(enable);
}

void ConfigDialog::slotItemClicked(QListViewItem *item)
{
// fixme: this code was used so that checkboxes can be
// enabled/disables with a single click on that line,
// but it fails when clicking on the checkbox itself :-(
//	QCheckListItem *check_item = (QCheckListItem*) item;
//	check_item->setOn(!check_item->isOn());
}


QString ConfigDialog::getFullFilename(QString rel_filename)
{
	QString filename = getenv("LANGUAGETOOL");
	if( filename.isEmpty() ) {
		// TODO: avoid code duplication (see Language::checkGrammar(())
		KMessageBox::error(0, i18n("The LANGUAGETOOL environment variable is not set. "
			"Please set it to the directory where LanguageTool is installed. "
			"Until then configuration will not be available."),
			// FIXME?: if empty configuration open, will this overwrite old config settings
			// with useless empty values?
			i18n("Undefined environment variable"));
		return QString::null;
	}
	if( ! filename.endsWith("/") ) {
		filename += "/";
	}
	filename = filename + rel_filename;
	return filename;
}

QDomDocument ConfigDialog::getDoc(QString filename)
{
	QDomDocument doc("grammar_rules");
	QFile f(filename);
	if ( !f.open( IO_ReadOnly ) ) {
		KMessageBox::error(0, i18n("The file '%1' could not be opened.").arg(filename),
			i18n("Error loading XML file"));
		return doc;
	}
	if ( !doc.setContent( &f ) ) {
		f.close();
		KMessageBox::error(0, i18n("The file '%1' could not be parsed. "
			"Please check that the file is a well-formed XML file, e.g. "
			"using the 'xmllint --noout %1' command.").arg(filename).arg(filename),
			i18n("Error parsing XML file"));
		return doc;
	}	
	f.close();
	return doc;
}

QDict<QString> ConfigDialog::getGrammarItems()
{
	kdDebug() << "getGrammarItems()" << endl;
	
	QDict<QString> items;

	QString filename = getFullFilename("rules/grammar.xml");
	if( filename == QString::null ) {
		// TODO: return null?
	}
	QDomDocument doc = getDoc(filename);
	QDomNodeList nodes = doc.elementsByTagName("rule");
	uint list_length = nodes.count();
	QString prev_id;
	for( uint i = 0; i < list_length; i++ ) {
		// find id:
		QDomNode node = nodes.item(i);
		QDomElement elem = node.toElement();
		QString id = elem.attribute("id");
		QDomNode rule_group;
		if( ! id ) {
			// id in rulegroup
			id = node.parentNode().toElement().attribute("id");
			rule_group = node.parentNode();
		}
		if( prev_id == id ) {
			// don't repeat the same name
			prev_id = id;
			continue;
		}
		prev_id = id;
		// find user-visible name:
		QString name = elem.attribute("name");
		if( ! name ) {
			name = rule_group.toElement().attribute("name");
		}
		// insert into list:
		items.insert(name.simplifyWhiteSpace(), new QString(id));
	}
	return items;
}

QDict<QString> ConfigDialog::getFalseFriendsItems(QListView *listview, 
	QStrList &rules, QDict<QCheckListItem> &checkboxes, bool has_false_friends_rules)
{
	kdDebug() << "getFalseFriendsItems()" << endl;
	
	QString filename = getFullFilename("rules/false_friends.xml");
	QDomDocument doc = getDoc(filename);
	
	QDict<QString> items;
	QDomNodeList nodes = doc.elementsByTagName("rulegroup");
	uint list_length = nodes.count();

	KConfig general("languagetool");
	general.setGroup("General");
	QString mother_tongue = general.readEntry("MotherTongue", "en");
	QString text_language = general.readEntry("TextLanguage", "en");

	for( uint i = 0; i < list_length; i++ ) {
		// find id:
		QDomNode node = nodes.item(i);
		QDomElement elem = node.toElement();
		QString id = elem.attribute("id");		// ?
		// find name:
		QDomNodeList sub_nodes = elem.elementsByTagName("pattern");
		uint sub_list_length = sub_nodes.count();
		QString name1, name2;
		for( uint j = 0; j < sub_list_length; j++ ) {
			QDomNode sub_node = sub_nodes.item(j);
			QDomElement sub_elem = sub_node.toElement();
			QString lang = sub_elem.attribute("lang");
				// TODO: better display the correct translation instead
				// of the false friend?
			if( name1.isEmpty() ) {
				name1 = sub_elem.text();
			} else {
				name2 = sub_elem.text();
			}
			// debug:
			//kdDebug() << "LANG:" << *id << ", text: " << sub_elem.text()<< endl;
		}
		// insert into list:
		QCheckListItem *item = new QCheckListItem(listview, name1, QCheckListItem::CheckBox);
		item->setText(1, name2);
		if( rules.find(id.latin1()) != -1 || ! has_false_friends_rules ) {
			item->setOn(true);
		}
		checkboxes.insert(id.simplifyWhiteSpace(), item);
	}
	return items;
}

QDict<QString> ConfigDialog::getWordsItems()
{
	kdDebug() << "getWordsItems()" << endl;
	
	QString filename = getFullFilename("rules/words.xml");
	QDomDocument doc = getDoc(filename);
	
	QDict<QString> items;
	QDomNodeList nodes = doc.elementsByTagName("rule");
	uint list_length = nodes.count();
	for( uint i = 0; i < list_length; i++ ) {
		// find id:
		QDomNode node = nodes.item(i);
		QDomElement elem = node.toElement();
		QString id = elem.attribute("id");
		// find name:
		QString name = elem.attribute("name");
		// insert into list:
		items.insert(name.simplifyWhiteSpace(), new QString(id));
	}
	return items;
}

RuleItem::RuleItem(QListView *listview, QString id_tmp, QString text) : 
	QCheckListItem(listview, text, QCheckListItem::CheckBox)
{
	id = id_tmp;
}

RuleItem::~RuleItem()
{
}

QString RuleItem::getId()
{
	return id;
}

#include "ConfigDialog.moc"
