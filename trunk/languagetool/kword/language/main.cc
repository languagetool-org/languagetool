/*
   $Id: main.cc,v 1.1 2003-08-24 22:36:19 dnaber Exp $
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

/*
FIXME(?):
-test configuration
-replacement destroys markup (bold etc)
-replacement destroys paragraphs (see "fixme")
-add ignore button again

TODO(?):
-re-check after configuration? start with first error then?
-use the same config no matter if called stand-alone??
 -> better declare stand-alone for testing only!?
-automatically place cursor on first bold word
-make the warning textbox a simple QLabel?
*/

#include <qdom.h>
#include <qtextstream.h>

#include <kbuttonbox.h>
#include <kconfig.h>
#include <kmessagebox.h>
#include <ktempfile.h>

#include "main.h"

/***************************************************
 *
 * Factory
 *
 ***************************************************/

K_EXPORT_COMPONENT_FACTORY( liblanguagetool, KGenericFactory<Language> );

/***************************************************
 *
 * Language *
 ***************************************************/

Language::Language(QObject* parent, const char* name, const QStringList &)
	: KDataTool(parent, name)
{
	
	m_text_has_errors = false;

	m_dialog = new KDialogBase(KJanusWidget::Plain, i18n("Style and Grammar Help"),
		KDialogBase::Help|KDialogBase::Ok|KDialogBase::Cancel, KDialogBase::Ok);
	m_dialog->setHelp(QString::null, "languagetool");
	m_dialog->resize(500, 200);

	QFrame *page = m_dialog->plainPage();
	QGridLayout *topLayout = new QGridLayout(page, 2, 3, KDialog::marginHint(), KDialog::spacingHint());

	m_sentencetext = new QTextEdit(page);
	QLabel *label1 = new QLabel(m_sentencetext, i18n("&Sentence:"), page);
	topLayout->addWidget(label1, 0, 0, Qt::AlignTop);
	topLayout->addWidget(m_sentencetext, 0, 1);

	m_msgtext = new QTextBrowser(page);
	QLabel *label2 = new QLabel(m_msgtext, i18n("Warnings:"), page);
	topLayout->addWidget(label2, 1, 0, Qt::AlignTop);
	topLayout->addWidget(m_msgtext, 1, 1);
	// FIXME: still get the "no mimesource" warning and the text
	// disappears:
	disconnect(m_msgtext, SIGNAL(linkClicked(const QString &)), 0, 0);
	disconnect(m_msgtext, SIGNAL(anchorClicked(const QString &, const QString &)), 0, 0);
	connect(m_msgtext, SIGNAL(linkClicked(const QString &)), this, SLOT(slotLinkClicked(const QString &)));
	//m_msgtext->mimeSourceFactory()->addFilePath("/");

	KButtonBox *buttonBox = new KButtonBox(page, Vertical);
	//prev_button = buttonBox->addButton(i18n("&Previous"), this, SLOT(previous()));
	//prev_button->setEnabled(false);
	//next_button = buttonBox->addButton(i18n("&Next"), this, SLOT(next()));
	//next_button->setEnabled(false);
	// fixme:
	//ignore_button = buttonBox->addButton(i18n("&Ignore errors of this kind"), this, SLOT(ignore()));
	//ignore_button->setEnabled(false);
	buttonBox->addButton(i18n("&Check"), this, SLOT(checkGrammar()));
	buttonBox->addButton(i18n("C&onfigure..."), this, SLOT(config()));
	buttonBox->layout();
	topLayout->addMultiCellWidget(buttonBox, 0, 1, 2, 2);

	reparseConfig();
	
	// connection to the server:
	m_socket = new QSocket();
	connect(m_socket, SIGNAL(error(int)), this, SLOT(slotError(int)));
	connect(m_socket, SIGNAL(connected()), this, SLOT(slotConnected()));
	connect(m_socket, SIGNAL(readyRead()), this, SLOT(slotReadyRead()));
	connect(m_socket, SIGNAL(connectionClosed()), this, SLOT(slotConnectionClosed()));
	
}


Language::~Language()
{
	if( m_dialog ) {
		delete m_dialog;
	}
}


bool Language::run(const QString& command, void* data, const QString& datatype, const QString& mimetype)
{

	if ( command != "language" && command != "language_standalone" ) {
		kdDebug(31000) << "The language tool does only accept the command 'language'" << endl;
		kdDebug(31000) << "   The command " << command << " is not accepted" << endl;
		return FALSE;
	}
	// Check whether we can accept the data
	if ( datatype != "QString" ) {
		kdDebug(31000) << "The language tool only accepts datatype QString" << endl;
		return FALSE;
	}
	if ( ! (mimetype == "text/plain" || mimetype == "application/x-qrichtext") ) {
		kdDebug(31000) << "The language tool only accepts mimetypes application/x-qrichtext and text/plain" << endl;
		return FALSE;
	}

	// Get filename or data:
	m_buffer = *((QString *)data);
	// Escape HTML-like markup. The parser will expand these later, so this is the
	// way it works:
	// "&nbsp;" --here--> "&amp;nbsp;" --parser--> "&nbsp;" --processExited()--> "&amp;nbsp;"
	// The last form can be put into an richtext widget without being interpreted.
	// TODO: still used???
	m_buffer.replace(QRegExp("&"), "&amp;");
	m_buffer.replace(QRegExp("<"), "&lt;");
	m_buffer.replace(QRegExp(">"), "&gt;");
	//m_buffer.replace(QRegExp("\n"), "<br/>");

	kdDebug(31000) << "m_buffer: '" << m_buffer << "'" << endl;
	checkGrammar(m_buffer);
	if( m_dialog->exec() == QDialog::Accepted ) {
		/*
		// build result
		rememberCurrentSentence();
		QString joined_sentences = "";
		QValueList<TestedSentence>::iterator it;
		for ( it = m_sentences.begin(); it != m_sentences.end(); ++it ) {
			joined_sentences += (*it).getNewSentence();
		}
		*((QString*)data) = joined_sentences;
		*/
		QString s = getPlainText();
		*((QString*)data) = s;
	}

	return TRUE;
}


QString Language::getPlainText()
{
	QString s;
	s = m_sentencetext->text();
	QRegExp re("<.*>");		// remove richtext
	re.setMinimal(true);
	s = s.replace(re, "");
	s = s.stripWhiteSpace();
	return s;
}

void Language::checkGrammar()
{
	QString s = getPlainText();
	// re-check possibly corrected text:
	checkGrammar(s);
}

/**
 * Connect to a server that checks the string. Get
 * the XML result in slotReadyRead().
 */
void Language::checkGrammar(QString text)
{
	kdDebug(31000) << "checkGrammar()" << endl;
	QApplication::setOverrideCursor(KCursor::waitCursor());

	// FIXME: use these...
	if( ! m_text_language.isEmpty() ) {
		//*m_proc << "--textlanguage" << m_text_language;
	}
	if( ! m_mother_tongue.isEmpty() ) {
		//*m_proc << "--mothertongue" << m_mother_tongue;
	}
	
	if( ! m_grammar_rules.isEmpty() ) {
		//*m_proc << "--grammar" << m_grammar_rules;
	} else {
		//*m_proc << "--grammar" << "_NO_RULE";
	}
	if( ! m_false_friends_rules.isEmpty() ) {
		//*m_proc << "--falsefriends" << m_false_friends_rules;
	} else {
		//*m_proc << "--falsefriends" << "_NO_RULE";
	}
	if( ! m_words_rules.isEmpty() ) {
		//*m_proc << "--words" << m_words_rules;
	} else {
		//*m_proc << "--words" << "_NO_RULE";
	}
	
	if( m_max_sentence_length > 0 ) {
		//*m_proc << "--max-sentence-length" << QString::number(m_max_sentence_length);
	}

	m_msgtext->setText(i18n("Checking text..."));

	m_socket->connectToHost("127.0.0.1", 50100);
	kdDebug(31000) << "Sending text: '" << text << "'" << endl;
	QTextStream os(m_socket);
	os << text << "\n";		// "\n" seems to be necessary...
	
	kdDebug(31000) << "# active grammar: " << m_grammar_rules << endl;
	kdDebug(31000) << "# active false friend rules: " << m_false_friends_rules << endl;
	kdDebug(31000) << "# active words: " << m_words_rules << endl;
	kdDebug(31000) << "# m_mother_tongue: " << m_mother_tongue << endl;
	kdDebug(31000) << "# textlanguage: " << m_text_language << endl;
	kdDebug(31000) << "# m_max_sentence_length: " << m_max_sentence_length << endl;
	m_buffer = text;
}

void Language::slotError(int err)
{
	QApplication::restoreOverrideCursor();
	if( err == QSocket::ErrConnectionRefused ) {
	    kdDebug(31000) << "slotError: connection refused" << endl;		
		//startServer();
	} else if( err == QSocket::ErrHostNotFound ) {
	    kdDebug(31000) << "slotError: host not found" << endl;		
	} else if( err == QSocket::ErrSocketRead ) {
	    kdDebug(31000) << "slotError: socket read failed" << endl;		
	} else {
	    kdDebug(31000) << "slotError: " << err << endl;
	}
	//delete m_socket;
	//m_socket = 0;
}

void Language::slotConnected()
{
	// do nothing
}

void Language::slotReadyRead()
{
    kdDebug(31000) << "slotReadyRead()" << endl;

	if( ! m_socket ) {
	    kdDebug(31000) << "Error: NO SOCKET!" << endl;
		return;
	}
	QString s;
	while( m_socket->canReadLine() ) {
	    QString t = m_socket->readLine();
		//kdDebug(31000) << "REPLY:" << t << endl;
		s = s + t; 
	}

    m_reply = s;
	QApplication::restoreOverrideCursor();

}

void Language::slotConnectionClosed()
{
    kdDebug(31000) << "slotConnectionClosed()" << endl;
	m_socket->close();
	
    kdDebug(31000) << "REPLY: " << m_reply << endl;

	QDomDocument doc("output");
	if ( !doc.setContent(m_reply) ) {
		KMessageBox::error(0, i18n("The server's output could not be parsed."),
			i18n("XML Parsing Error"));
		QApplication::restoreOverrideCursor();
		return;
	}

	QDomNodeList list = doc.elementsByTagName("error");
	uint list_length = list.count();
	bool error_found = false;
	for( uint i = 0; i < list_length; i++ ) {
		kdDebug(31000) << "Found node <error>" << endl;
		QDomNode node = list.item(i);
		QDomElement elem = node.toElement();

		// FIXME/TODO: test with <, > and & etc.

		if( !elem.isNull() ) {
			error_found = true;
			int from = elem.attribute("from").toInt();
			int to = elem.attribute("to").toInt();
			QString error_text;
			kdDebug(31000) << "Found element <error>: " << from << ", " << 
				to << ", " << error_text << endl;
			if( elem.attribute("word") == QString::null ) {
				// grammar error
				error_text = toText(elem, from, to).simplifyWhiteSpace();
			} else {
				// spelling error
				error_text = "Unknown word <b>" +elem.attribute("word")+ "</b>. ";
				QStringList suggestions = QStringList::split(",", elem.attribute("corrections"));
				if( suggestions.count() > 0 ) {
					error_text += "Suggested corrections: ";
					for( QStringList::Iterator it = suggestions.begin(); it != suggestions.end(); ++it ) {
						error_text += QString("<a href=\"%1,%2,%3\">%4</a> ").arg(from).
							arg(to).arg(*it).arg(*it);
					}
				} else {
					error_text += "No corrections.";
				}
			}
			m_sentencetext->setEnabled(true);
			m_sentencetext->setText("<qt>" + m_buffer + "</qt>");
			m_sentencetext->setSelection(0,from, 0,to);
			m_sentencetext->setBold(true);
			m_sentencetext->setColor("red");
			m_sentencetext->selectAll(false);
			m_msgtext->setText(error_text);
			m_error_text = error_text;
		}
		break;		// for now, care about the errors one-by-one
	}

	if( ! error_found ) {
		//prev_button->setEnabled(false);
		//next_button->setEnabled(false);
		m_msgtext->setText(i18n("No errors or warnings have been found."));
		// ###FIXME: remove red parts in m_sentencetext
		m_text_has_errors = false;
	}

	QApplication::restoreOverrideCursor();

}


void Language::slotLinkClicked(const QString &url)
{
	kdDebug(31000) << "link clicked: " << url << endl;

	// ugly hack to work around the problem that Qt
	// tries to follow the link, even if the signal
	// is disconnected:
	m_msgtext->setText(m_error_text);
	
	QStringList params = QStringList::split(",", url);
	int from = params[0].toInt();
	int to = params[1].toInt();
	QString replacement = params[2];
	m_sentencetext->setSelection(0,from, 0,to);
	m_sentencetext->removeSelectedText();
	m_sentencetext->setBold(false);
	m_sentencetext->setColor("black");
	m_sentencetext->insert(replacement);

	checkGrammar();
}


/** Get all text content recursively. Text inside "em" elements 
  * is nested inside <a>...</a>. 
  */
QString Language::toText(QDomNode node, int from, int to, QString text)
{
	QDomText t = node.toText();
	if ( !t.isNull() ) {
		text += t.data();
	} else if( node.isElement() ) {
		//QDomElement el = elem.toElement();
		QString ntext;
		for( QDomNode n = node.firstChild(); !n.isNull(); n = n.nextSibling() ) {
			ntext += toText(n, from, to, text);
		}
		if( node.nodeName() == "em" ) {
			text = QString("<a href=\"%1,%2,%3\"><b>%4</b></a>").arg(from).
				arg(to).arg(ntext).arg(ntext);
		} else {
			text += ntext;
		}
	}
	return text;
}


void Language::reparseConfig()
{
	KConfig general("languagetool");
	general.setGroup("General");
	m_mother_tongue = general.readEntry("MotherTongue", "en");
	m_text_language = general.readEntry("TextLanguage", "en");
	// the next three options are lists, but we need them as strings:
	if( general.readBoolEntry("EnableGrammar", true) ) {
		m_grammar_rules = general.readEntry("GrammarRules", "");
	}
	if( general.readBoolEntry("EnableFalseFriends", true) ) {
		m_false_friends_rules = general.readEntry("FalseFriendsRules", "");
	}
	if( general.readBoolEntry("EnableWords", true) ) {
		m_words_rules = general.readEntry("WordsRules", "");
	}
	m_max_sentence_length = general.readNumEntry("MaxSentenceLength", 0);
}

/*
void Language::previous()
{
	rememberCurrentSentence();
	// go to previous error:
	m_errors_ct--;
	showError();
}

void Language::next()
{
	rememberCurrentSentence();
	// go to next error:
	m_errors_ct++;
	showError();
}
*/

void Language::rememberCurrentSentence()
{
	// remember user input:
	kdDebug(31000) << "m_errors_ct: " << m_errors_ct << endl;
	kdDebug(31000) << "m_errors[m_errors_ct]: " << m_errors[m_errors_ct] << endl;

	QString str = m_sentencetext->text();
	/*QRegExp re("> \n");
	str.replace(re, "> ");
	QRegExp re2(">\n");
	str.replace(re2, ">");*/
	//re.setMinimal(true);
	//re.setPattern("<.*>");	// remove error annotation
	//str.replace(re, "");

	//kdDebug(31000) << "##old text: " << m_sentencetext->text() << "'" << endl;
	//kdDebug(31000) << "##new text: " << str << endl;
	m_sentences[m_errors[m_errors_ct]].setSentence(str);
}

/* void Language::ignore()
{
	// TODO: ignore current ID
	m_errors_ct++;
	showError();
} */

void Language::config()
{
	kdDebug(31000) << "config()" << endl;
	ConfigDialog *cfg = new ConfigDialog();
	// fixme: delete cfg?!
}

/* ---------------------------------------------------------------- */
// this is needed by QValueList<Error>
TestedSentence::TestedSentence()
{
	m_sentence = "[internal]";	// if this appears to the user, there's a bug
	m_errorlist = "[internal]";
}

TestedSentence::TestedSentence(QString sentence, QStringList errors)
{
	m_sentence = sentence;
	m_errorlist = errors;
}

TestedSentence::~TestedSentence()
{
}

QString TestedSentence::getSentence()
{
	return m_sentence;
}

void TestedSentence::setSentence(QString s)
{
	m_sentence = s;
}

QString TestedSentence::getNewSentence()
{
	QString str = m_sentence;
	bool from_gui = false;
	kdDebug(31000) << "#####" << str << "#" << endl;
	if( str.find("<qt>") != -1 ) {
	kdDebug(31000) << "#####!!" << endl;
		from_gui = true;
	}
	QRegExp re("<.*>");  // remove error annotation
	re.setMinimal(true);
	str = str.replace(re, "");
	str.replace(QRegExp("&nbsp;"), " ");
	// work around Qt's invented whitespace:
	kdDebug(31000) << "##(1)'" << str << "'" << endl;
	if( from_gui ) {
		str.replace(QRegExp("\\n+$"), "");
	}
	kdDebug(31000) << "##(2)'" << str << "'" << endl;
	//kdDebug(31000) << "# str:" << str << "#" << endl;
	return str;
}

QStringList TestedSentence::getErrorList()
{
	return m_errorlist;
}

bool TestedSentence::hasErrors()
{
	return (m_errorlist.count() > 0);
}

#include "main.moc"
