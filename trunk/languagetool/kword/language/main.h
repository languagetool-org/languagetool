/*
   $Id: main.h,v 1.2 2003-08-25 19:16:58 dnaber Exp $
   This file is part of the KDE project
   Copyright (C) 2001,2002 Daniel Naber <daniel.naber@t-online.de>
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

#ifndef __main_h__
#define __main_h__

#include <qapplication.h>
#include <qlabel.h>
#include <qlayout.h>
#include <qlistbox.h>
#include <qcombobox.h>
#include <qobject.h>
#include <qpushbutton.h>
#include <qregexp.h>
#include <qstring.h>
#include <qstringlist.h>
#include <qtextedit.h>
#include <qtextbrowser.h>
#include <qwidget.h>
#include <qvbox.h>
#include <qhbox.h>
#include <qvaluelist.h>
// dnaber:
#include <qsocket.h>

#include <kapplication.h>
#include <kcombobox.h>
#include <kcursor.h>
#include <kdatatool.h>
#include <kdebug.h>
#include <kdialogbase.h>
#include <kgenericfactory.h>
#include <klibloader.h>
#include <klocale.h>
#include <kmessagebox.h>
#include <kprocess.h>
#include <krun.h>
#include <kstandarddirs.h>
#include <kurl.h>

//#include <langtool.h>

#include <ConfigDialog.h>

class TestedSentence
{
public:
	TestedSentence(QString sentence, QStringList errors);
	TestedSentence();
	~TestedSentence();
	QStringList getErrorList();
	QString getSentence();
	void setSentence(QString s);
	QString getNewSentence();
	bool hasErrors();
	
protected:
	QString m_sentence;
	QStringList m_errorlist;
};


class Language : public KDataTool
{
	Q_OBJECT

public:
	Language(QObject* parent, const char* name, const QStringList &);
	~Language();
	virtual bool run(const QString& command, void* data, 
		const QString& datatype, const QString& mimetype);

protected slots:
	// dnaber:
	void checkGrammar();
	void slotError(int err);
	void slotConnected();
	void slotReadyRead();
	void slotConnectionClosed();
	
	void slotLinkClicked(const QString &url);
	
	void reparseConfig();
	//void previous();
	//void next();
	//void ignore();
	void config();

protected:
	QString getPlainText();
	void rememberCurrentSentence();
	void checkGrammar(QString text);
	//void showError();

	QString toText(QDomNode node, int from, int to, QString text="");

	bool m_text_has_errors;

	QString m_buffer;	// the user's text
	
	KDialogBase *m_dialog;
	
	QTextEdit *m_sentencetext;
	QTextBrowser *m_msgtext;
	//QPushButton *next_button, *prev_button;
	QPushButton *ignore_button;
	
	QString m_mother_tongue, m_text_language;
	QString m_grammar_rules, m_false_friends_rules, m_words_rules;
	int m_max_sentence_length;
	bool m_whitespace_rule;
	bool m_articles_rule;
	
	QValueList<TestedSentence> m_sentences;
	QValueList<int> m_errors;
	uint m_errors_ct;
	//QValueList<TestedSentence>::iterator m_errors_it;

	QSocket *m_socket;
	QString m_reply;
	QString m_error_text;
	bool new_config;
	
};

#endif
