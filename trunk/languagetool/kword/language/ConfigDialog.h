/*
   $Id: ConfigDialog.h,v 1.2 2003-08-25 19:17:25 dnaber Exp $
   This file is part of the KDE project
   Copyright (C) 2002 Daniel Naber <daniel.naber@t-online.de>
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

#ifndef __languagetoolconfigdialog_h__
#define __languagetoolconfigdialog_h__

#include <qcheckbox.h>
#include <qcombobox.h>
#include <qdom.h>
#include <qdict.h>
#include <qlistview.h>
#include <qspinbox.h>
#include <qvaluelist.h>

#include <kdialogbase.h>

class RuleItem;

class ConfigDialog : public KDialogBase
{

Q_OBJECT

public:
	ConfigDialog();
	~ConfigDialog();
	void saveConfig();

protected slots:
	void slotToggleGrammar(bool enable);
	void slotToggleFalseFriends(bool enable);
	void slotToggleWords(bool enable);
	void slotToggleSentenceLength(bool enable);
	void slotToggleWhitespaceCheck(bool enable);
	void slotToggleArticleCheck(bool enable);

	void slotItemClicked(QListViewItem *item);
	
protected:
	KDialogBase *m_dialog;

private:
	
	QDict<QString> getGrammarItems();
	QDict<QString> getFalseFriendsItems(QListView *listview,
		QStrList &rules, QDict<QCheckListItem> &checkboxes, bool has_false_friends_rules);
	QDict<QString> getWordsItems();
	
	QDict<QCheckListItem> m_grammar_checkboxes;
	QDict<QCheckListItem> m_false_friends_checkboxes;
	QDict<QCheckListItem> m_words_checkboxes;

	QString getFullFilename(QString rel_filename);
	QDomDocument getDoc(QString filename);
	
	QPtrList<QCheckBox> boxes;
	QCheckBox *check1, *check2, *check3, *check_sentence_length, 
		*check_whitespace, *check_articles;
	QSpinBox *sentence_spinbox;
	QListView *listview1, *listview2, *listview3;
	QComboBox *combo_mother_tongue, *combo_text_language;
};

class RuleItem : public QCheckListItem
{
public:
	RuleItem(QListView *listview, QString id, QString text);
	~RuleItem();
	QString getId();
private:
	QString id;
};
#endif
