/* This file is part of the KDE project
   Copyright (C) 2002 David Faure <david@mandrakesoft.com>
                 2002 Laurent Montel <lmontel@mandrakesoft.com>
                 2003 Daniel Naber <daniel.naber@t-online.de>

   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Library General Public
   License as published by the Free Software Foundation; either
   version 2 of the License, or (at your option) any later version.

   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Library General Public License for more details.

   You should have received a copy of the GNU Library General Public License
   along with this library; see the file COPYING.LIB.  If not, write to
   the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.
*/


#include "koBgSpellCheck.h"
#include "koBgSpellCheck.moc"
#include <qtimer.h>
#include <kdebug.h>
#include <kospell.h>
#include <ksconfig.h>
#include <kotextobject.h>
#include <klocale.h>

//dnaber:
#include <qregexp.h>
#include <kprocess.h>
#include <kmessagebox.h>

#define DEBUG_BGSPELLCHECKING

KoBgSpellCheck::KoBgSpellCheck()
{
    m_pKSpellConfig=0L;
    m_bDontCheckUpperWord=false;
    m_bSpellCheckEnabled=false;
    m_bDontCheckTitleCase=false;
    m_bSpellCheckConfigure=false;
    m_bgSpell.currentTextObj=0L;
    m_bgSpell.needsRepaint=false;
	m_server_starts = 0;	// count tries to start 'languagetool'
	m_socket = new QSocket();
	connect(m_socket, SIGNAL(error(int)), this, SLOT(slotError(int)));
	connect(m_socket, SIGNAL(connected()), this, SLOT(slotConnected()));
	connect(m_socket, SIGNAL(readyRead()), this, SLOT(slotReadyRead()));
	connect(m_socket, SIGNAL(connectionClosed()), this, SLOT(slotConnectionClosed()));
}

KoBgSpellCheck::~KoBgSpellCheck()
{
	delete m_socket;
    delete m_pKSpellConfig;
}

void KoBgSpellCheck::addPersonalDictonary( const QString & word )
{
}

void KoBgSpellCheck::spellCheckParagraphDeleted( KoTextParag *_parag,  KoTextObject *obj)
{
    if ( m_bgSpell.currentTextObj == obj && m_bgSpell.currentParag == _parag)
    {
        stopSpellChecking();
        startBackgroundSpellCheck();
    }
}


void KoBgSpellCheck::enableBackgroundSpellCheck( bool b )
{
    m_bSpellCheckEnabled=b;
    startBackgroundSpellCheck(); // will enable or disable
}

void KoBgSpellCheck::setIgnoreUpperWords( bool b)
{
    stopSpellChecking();
    m_bDontCheckUpperWord = b;
    startBackgroundSpellCheck();
}

void KoBgSpellCheck::setIgnoreTitleCase( bool b)
{
    stopSpellChecking();
    m_bDontCheckTitleCase = b;
    startBackgroundSpellCheck();
}

void KoBgSpellCheck::addIgnoreWordAll( const QString & word)
{
    if( m_spellListIgnoreAll.findIndex( word )==-1)
        m_spellListIgnoreAll.append( word );
    stopSpellChecking();
    spellConfig()->setIgnoreList( m_spellListIgnoreAll );
    startBackgroundSpellCheck();
}

void KoBgSpellCheck::addIgnoreWordAllList( const QStringList & list)
{
    m_spellListIgnoreAll.clear();
    stopSpellChecking();
    spellConfig()->setIgnoreList( list );
    startBackgroundSpellCheck();
}

void KoBgSpellCheck::clearIgnoreWordAll( )
{
    m_spellListIgnoreAll.clear();
    stopSpellChecking();
    spellConfig()->setIgnoreList( m_spellListIgnoreAll );
    startBackgroundSpellCheck();
}

void KoBgSpellCheck::startBackgroundSpellCheck()
{
    if ( !m_bSpellCheckEnabled )
        return;
    //re-test text obj
    if ( !m_bgSpell.currentTextObj )
    {
        m_bgSpell.currentTextObj = nextTextObject(m_bgSpell.currentTextObj );
    }
    if ( !m_bgSpell.currentTextObj )
    {
        QTimer::singleShot( 1000, this, SLOT( startBackgroundSpellCheck() ) );
        return;
    }
#ifdef DEBUG_BGSPELLCHECKING
    kdDebug(32500) << "KoBgSpellCheck::startBackgroundSpellCheck" << endl;
#endif

    m_bgSpell.currentParag = m_bgSpell.currentTextObj->textDocument()->firstParag();
    nextParagraphNeedingCheck();

    //kdDebug(32500) << "fs=" << m_bgSpell.currentTextObj << " parag=" << m_bgSpell.currentParag << endl;

    if ( !m_bgSpell.currentTextObj || !m_bgSpell.currentParag ) {
        if ( m_bgSpell.currentTextObj )
        {
            if ( (m_bgSpell.currentTextObj->textDocument()->firstParag() == m_bgSpell.currentTextObj->textDocument()->lastParag()) && m_bgSpell.currentTextObj->textDocument()->firstParag()->length() <= 1)
                m_bgSpell.currentTextObj->setNeedSpellCheck(false);
        }
        // Might be better to launch again upon document modification (key, pasting, etc.) instead of right now
        //kdDebug(32500) << "KWDocument::startBackgroundSpellCheck nothing to check this time." << endl;
        QTimer::singleShot( 1000, this, SLOT( startBackgroundSpellCheck() ) );
        return;
    }

	spellCheckerReady();
}

void KoBgSpellCheck::spellCheckerReady()
{
    //necessary to restart to beginning otherwise we don't check
    //other parag
    if (m_bgSpell.currentTextObj) {
        m_bgSpell.currentParag = m_bgSpell.currentTextObj->textDocument()->firstParag();
	}

    //kdDebug(32500) << "KWDocument::spellCheckerReady" << endl;
    QTimer::singleShot( 10, this, SLOT( spellCheckNextParagraph() ) );
}

// Input: currentTextObj non-null, and currentParag set to the last parag checked
// Output: currentTextObj+currentParag set to next parag to check. Both 0 if end.
void KoBgSpellCheck::nextParagraphNeedingCheck()
{
    kdDebug(32500) << "KoBgSpellCheck::nextParagraphNeedingCheck " <<m_bgSpell.currentTextObj <<endl;
    if ( !m_bgSpell.currentTextObj ) {
        m_bgSpell.currentParag = 0L;
        return;
    }

    // repaint the textObject here if it requires it
    // (perhaps there should be a way to repaint just a paragraph.... - JJ)
    if(m_bgSpell.needsRepaint)
    {
         slotRepaintChanged( m_bgSpell.currentTextObj );
         m_bgSpell.needsRepaint=false;
    }

    KoTextParag* parag = m_bgSpell.currentParag;
    if ( parag && parag->string() && parag->string()->needsSpellCheck() )
        return;

    if ( parag && parag->next() )
        parag = parag->next();
    // Skip any unchanged parags
    while ( parag && !parag->string()->needsSpellCheck() )
        parag = parag->next();
    while ( parag && parag->length() <= 1 ) // empty parag
    {
        parag->string()->setNeedsSpellCheck( false ); // nothing to check
        while ( parag && !parag->string()->needsSpellCheck() ) // keep looking
            parag = parag->next();
    }
    if ( parag )
        m_bgSpell.currentParag = parag;
    else
        m_bgSpell.currentParag = 0L;

    if( !m_bgSpell.currentParag)
    {
        KoTextObject *obj=m_bgSpell.currentTextObj;
        //kdDebug()<<" obj :"<<obj<<endl;
        m_bgSpell.currentTextObj=nextTextObject( m_bgSpell.currentTextObj );
        //kdDebug()<<" m_bgSpell.currentTextObj !"<<m_bgSpell.currentTextObj<<endl;
        if ( m_bgSpell.currentTextObj && m_bgSpell.currentTextObj!=obj)
        {
            m_bgSpell.currentParag = m_bgSpell.currentTextObj->textDocument()->firstParag();
        }
        else
        {
            if ( m_bgSpell.currentParag )
                m_bgSpell.currentParag->string()->setNeedsSpellCheck( false );
            if ( m_bgSpell.currentTextObj )
                m_bgSpell.currentTextObj->setNeedSpellCheck( false );
            m_bgSpell.currentParag = 0L;
        }
    }
    //kdDebug()<<" KoBgSpellCheck::nextParagraphNeedingCheck() : m_bgSpell.currentParag :"<<m_bgSpell.currentParag<<endl;
    kdDebug(32500) << " currentParag=" << m_bgSpell.currentParag <<endl;

}

// fixme: rename
void KoBgSpellCheck::spellCheckNextParagraph()
{
    kdDebug(32500) << "KoBgSpellCheck::spellCheckNextParagraph" << endl;
    nextParagraphNeedingCheck();
    kdDebug(32500) << "fs=" << m_bgSpell.currentTextObj << " parag=" << m_bgSpell.currentParag << endl;

    if ( !m_bgSpell.currentTextObj || !m_bgSpell.currentParag )
    {
        kdDebug(32500) << "KoBgSpellCheck::spellCheckNextParagraph scheduling restart" << endl;
        // We arrived to the end of the paragraphs. Jump to startBackgroundSpellCheck,
        // it will check if we still have something to do.
        QTimer::singleShot( 100, this, SLOT( startBackgroundSpellCheck() ));
        return;
    }
    kdDebug(32500) << "KoBgSpellCheck::spellCheckNextParagraph spell checking parag " << m_bgSpell.currentParag->paragId() << endl;

    // Now check that paragraph
    QString text = m_bgSpell.currentParag->string()->toString();
    text.remove( text.length() - 1, 1 ); // trailing space

	// dnaber:
	if( text.isEmpty() ) {
	    kdDebug(32500) << "TEXT EMPTY" << endl;
    	if(m_bgSpell.currentParag)
        	m_bgSpell.currentParag->string()->setNeedsSpellCheck(false);
    	if( m_bgSpell.currentTextObj && m_bgSpell.currentParag == m_bgSpell.currentTextObj->textDocument()->lastParag() )
        	m_bgSpell.currentTextObj->setNeedSpellCheck(false);
		QTimer::singleShot(10, this, SLOT(spellCheckNextParagraph()));
	} else {
		m_socket->connectToHost("127.0.0.1", 50100);
		kdDebug() << "Sending text: '" << text << "'" << endl;
		QTextStream os(m_socket);
		os << text << "\n";		// "\n" seems to be necessary...
	}
}

// dnaber:
void KoBgSpellCheck::slotConnected()
{
}

// dnaber:
void KoBgSpellCheck::startServer()
{
	if( m_server_starts > 0 ) {
	    kdDebug(32500) << "##Won't start 'languagetool' (" << m_server_starts << ")" << endl;
		return;
	}
	KProcess *proc = new KProcess();
    kdDebug(32500) << "##Trying to start 'languagetool'..." << endl;		
    proc->clearArguments();
    *proc << "languagetool";
    m_server_starts++;
    if( ! proc->start(KProcess::NotifyOnExit, KProcess::AllOutput) ) {
        KMessageBox::error(0, i18n("Failed to execute 'languagetool'. Is it not in your $PATH?"));
        kdDebug(32500) << "##'languagetool' start failed" << endl;		
    } else {
	    kdDebug(32500) << "##'languagetool' started" << endl;		
	    QTimer::singleShot(2000, this, SLOT(spellCheckNextParagraph()));
	}
}

// dnaber:
void KoBgSpellCheck::slotError(int err)
{
	if( err == QSocket::ErrConnectionRefused ) {
	    kdDebug(32500) << "slotError: connection refused" << endl;		
		startServer();
	} else if( err == QSocket::ErrHostNotFound ) {
	    kdDebug(32500) << "slotError: host not found" << endl;		
	} else if( err == QSocket::ErrSocketRead ) {
	    kdDebug(32500) << "slotError: socket read failed" << endl;		
	} else {
	    kdDebug(32500) << "slotError: " << err << endl;
	}
	//delete m_socket;
	//m_socket = 0;
}

// dnaber:
void KoBgSpellCheck::slotReadyRead()
{
    kdDebug(32500) << "slotReadyRead()" << endl;
    KoTextObject *fs = m_bgSpell.currentTextObj;
    if ( !fs ) return;
    KoTextParag *parag = m_bgSpell.currentParag;
    if ( !parag ) return;

    //kdDebug(32500) << "KoBgSpellCheck::slotReadyRead() parag=" << parag << " (id=" << parag->paragId() << ", length=" << parag->length() << ") pos=" << pos << " length=" << old.length() << endl;
    //kdDebug(32500) << "KoBgSpellCheck::slotReadyRead() parag=" << parag << " id=" << parag->paragId() << ", length=" << parag->length() << endl;
	if( ! m_socket ) {
	    kdDebug(32500) << "Error: NO SOCKET!" << endl;
		return;
	}
	QString s;
    //kdDebug(32500) << "REPLY??" << endl;
	while( m_socket->canReadLine() ) {
	    QString t = m_socket->readLine();
		//kdDebug(32500) << "REPLY:" << t << endl;
		s = s + t; 
	}
    kdDebug(32500) << "REPLY:" << s << endl;

    // First remove any misspelled format from the paragraph
    // - otherwise we'd never notice words being ok again :)
    KoTextStringChar *ch = m_bgSpell.currentParag->at( 0 );
    KoTextFormat format( *ch->format() );
    format.setMisspelled( false );
    m_bgSpell.currentParag->setFormat( 0, m_bgSpell.currentParag->length()-1, &format, true, KoTextFormat::Misspelled );
kdDebug(32500) << "xxxx:" << m_bgSpell.currentParag->length()-1 << endl;
    format.setStyleGrammarWarning( false );
    m_bgSpell.currentParag->setFormat( 0, m_bgSpell.currentParag->length()-1, &format, true, KoTextFormat::StyleGrammarWarning );


	QRegExp rx("<error from=\"([^\"]*)\" to=\"([^\"]*)\" word=\"([^\"]*)\" corrections=\"([^\"]*)\"/>");
	rx.setMinimal(true);
	int offset = 0;
	while( (offset = rx.search(s, offset)) != -1 ) {
	    //kdDebug(32500) << "FROM=" << rx.cap(1) << endl;
	    //kdDebug(32500) << "TO=" << rx.cap(2) << endl;
	    //kdDebug(32500) << "WORD=" << rx.cap(3) << endl;
	    //kdDebug(32500) << "CORR=" << rx.cap(4) << endl;
		int pos_from = rx.cap(1).toInt();
		int pos_to = rx.cap(2).toInt();
    	KoTextStringChar *ch = parag->at(pos_from);
   		KoTextFormat format(*ch->format());
   		format.setMisspelled(true);
    	parag->setFormat(pos_from, pos_to-pos_from, &format, true, KoTextFormat::Misspelled);
		offset += rx.matchedLength();
	}

	QRegExp rx2("<error from=\"([^\"]*)\" to=\"([^\"]*)\">(.*)</error>");
	rx2.setMinimal(true);
	offset = 0;
	while( (offset = rx2.search(s, offset)) != -1 ) {
	    //kdDebug(32500) << "FROM=" << rx2.cap(1) << endl;
	    //kdDebug(32500) << "TO=" << rx2.cap(2) << endl;
	    //kdDebug(32500) << "COMM=" << rx2.cap(3) << endl;
		int pos_from = rx2.cap(1).toInt();
		int pos_to = rx2.cap(2).toInt();
		
    	KoTextStringChar *ch = parag->at(pos_from);
   		KoTextFormat format(*ch->format());
   		format.setStyleGrammarWarning(true);
    	parag->setFormat(pos_from, pos_to-pos_from, &format, true, KoTextFormat::StyleGrammarWarning);
		offset += rx2.matchedLength();
	}

    // set the repaint flags
    parag->setChanged(true);
    m_bgSpell.needsRepaint = true;
}

// dnaber:
void KoBgSpellCheck::slotConnectionClosed()
{
    kdDebug(32500) << "slotConnectionClosed()" << endl;

	m_socket->close();

	// from spellCheckerDone():
    if(m_bgSpell.currentParag)
        m_bgSpell.currentParag->string()->setNeedsSpellCheck(false);
    if( m_bgSpell.currentTextObj && m_bgSpell.currentParag==m_bgSpell.currentTextObj->textDocument()->lastParag())
        m_bgSpell.currentTextObj->setNeedSpellCheck(false);
	QTimer::singleShot(10, this, SLOT(spellCheckNextParagraph()));
	
}


void KoBgSpellCheck::spellCheckerFinished()
{
    kdDebug(32500) << "--- KoBgSpellCheck::spellCheckerFinished ---" << endl;
    m_bgSpell.currentParag = 0;
    m_bgSpell.currentTextObj = 0;
}

KSpellConfig* KoBgSpellCheck::spellConfig()
{
  if ( !m_pKSpellConfig )
    m_pKSpellConfig = new KSpellConfig();
  return m_pKSpellConfig;
}

void KoBgSpellCheck::setKSpellConfig(KSpellConfig _kspell)
{
  (void)spellConfig();
  stopSpellChecking();

  m_pKSpellConfig->setNoRootAffix(_kspell.noRootAffix ());
  m_pKSpellConfig->setRunTogether(_kspell.runTogether ());
  m_pKSpellConfig->setDictionary(_kspell.dictionary ());
  m_pKSpellConfig->setDictFromList(_kspell.dictFromList());
  m_pKSpellConfig->setEncoding(_kspell.encoding());
  m_pKSpellConfig->setClient(_kspell.client());
  m_bSpellCheckConfigure = false;
  startBackgroundSpellCheck();
}

void KoBgSpellCheck::stopSpellChecking()
{
    kdDebug(32500) << "--- KoBgSpellCheck::stopSpellChecking() ---" << endl;
  m_bgSpell.currentParag = 0;
  m_bgSpell.currentTextObj = 0;
}
