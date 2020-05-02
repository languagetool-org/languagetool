#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  test.py
#   Convert a file which contains grammar rules into grammar xml format for LanguageTool
#   The text file contains linguistic rules from book of "Guide of Commons errors" by Marwan Albawab
# الملف معالج يدويا ومجهز للبرمجة
# الملف فيه الأعمدة التالية:
# * style pattern النمطة
# * correction المستبدل
# * note الملاحظة
# * Error  الخطأ
# * Correction التصحيح



import sys,re,string
import sys, getopt, os
scriptname = os.path.splitext(os.path.basename(sys.argv[0]))[0]
scriptversion = '0.1'
import pyarabic.araby as araby
AuthorName="Taha Zerrouki"

# Limit of the fields treatment

MAX_LINES_TREATED=1100000;


def usage():
# "Display usage options"
    print "(C) CopyLeft 2017, %s"%AuthorName
    print "Usage: %s -f filename [OPTIONS]" % scriptname
#"Display usage options"
    print "\t[-h | --help]\t\toutputs this usage message"
    print "\t[-v | --version]\tprogram version"
    print "\t[-f | --file= filename]\tinput file to %s"%scriptname
    print "\t[-l | --limit= limit_ number]\tthe limit of treated lines %s"%scriptname
    print "\r\nN.B. FILE FORMAT is descripted in README"
    print "\r\nThis program is licensed under the GPL License\n"


def grabargs():
#  "Grab command-line arguments"
    fname = ''
    limit=MAX_LINES_TREATED;
    if not sys.argv[1:]:
        usage()
        sys.exit(0)
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hv:f:l:",
                               ["help", "version", "file=","limit="],)
    except getopt.GetoptError:
        usage()
        sys.exit(0)
    for o, val in opts:
        if o in ("-h", "--help"):
            usage()
            sys.exit(0)
        if o in ("-v", "--version"):
            print scriptversion
            sys.exit(0)
        if o in ("-f", "--file"):
            fname = val
        if o in ("-l", "--limit"):
            try:
                limit = int(val);
            except:
                limit=MAX_LINES_TREATED;

            
    return fname,limit


                 
def main():
    filename,limit= grabargs()
    try:
        fl=open(filename);
    except:
        print " Error :No such file or directory: %s" % filename
        sys.exit(0)



    #abbrevated=False;
    field_number=2;
    cat_field_number=3;
    #skip the first line
    line=fl.readline().decode("utf");
    line=fl.readline().decode("utf");
    text=u""
    rule_table=[];
    nb_field=5;

    while line :
        line = line.strip('\n')
        if not line.startswith("#"):
            liste=line.split("\t");
            liste = [x.strip() for x in liste]
            if len(liste) >= nb_field:
                rule_table.append(liste);
        line=fl.readline().decode("utf8");
    fl.close();
    #limit=MAX_LINES_TREATED;
    idrule = 1
    for tuple_rule in rule_table[:limit]:
    #   
        rule ={}
        rule['pattern']        = tuple_rule[0].strip();
        rule['suggestions']    = tuple_rule[1].strip();
        rule['message']        = tuple_rule[2].strip();
        rule['wrong_example']  = tuple_rule[3].strip();
        rule['correct_example']  = tuple_rule[4].strip();

        print treat_rule(rule, idrule).encode('utf8')
        idrule += 1
        
def treat_rule(rule, idr):
    """ treat rule to be displayed as LT grammar XML
    
    XML format as:
        <rule>
        <pattern>
        <marker><token>ثلاثة</token></marker>
        <token postag='NFP'/>           
        </pattern>          
        <message>أتقصد <suggestion>ثلاث</suggestion>؟</message>
        الاسم المؤنث يسبق بعدد مذكر
        <example correction="ثلاثة"><marker>ثلاث</marker>أولاد</example>
        <example correction="ثلاث"><marker>ثلاثة</marker>بنات</example>
    </rule>
    
    input format is 
        rule['pattern']      ;
        rule['suggestions']    
        rule['message']        ;
        rule['wrong_example']  ;
        rule['correct_example'];    
    """
    
    pattern, message = treat_pattern(rule['pattern'], rule['suggestions'], rule['message'])
    example = treat_example(rule['wrong_example'], rule['correct_example'])
    text = u"""\t<rule id ='unsorted%03.d'>
\t\t<pattern>
\t\t%s
\t\t</pattern>
\t\t<message>%s</message>
\t\t%s
\t\t<!--  Wrong: %s -->
\t\t<!--Correct: %s -->
\t</rule>
    """%(idr, pattern, message, example, rule['wrong_example'], rule['correct_example'])
    return text
    
def treat_pattern(pattern, suggestions, message):
    """
    Extract infos and fields from input
    """
    tokens = araby.tokenize(pattern)
    patternxml = u"""<token>%s</token>"""%u"</token>\n\t\t<token>".join(tokens)
    sugs = suggestions.split('|')
    sugsxml = u"""\t\t<suggestion>%s</suggestion>"""%u"</suggestion>\n\t\t<suggestion>".join(sugs)
    messagexml = u"""يفضل أن يقال:\n%s\n%s"""%(sugsxml, message)
    return patternxml, messagexml

def treat_example(wrong_example, correct_example):
    """ create an element to represent an example of error """
    
    # split tokens
    correct_example = correct_example.split('/')[0]
    correct_tokens = araby.tokenize(correct_example)
    wrong_tokens   = araby.tokenize(wrong_example)
    
    correct_word ,   wrong_tokens = diff(wrong_tokens, correct_tokens)
    correct_word = u" ".join(correct_word)
    wrong_output  = u" ".join(wrong_tokens)
    example = u"<example correction='%s'>%s</example>\n"%(correct_word, wrong_output)
    return example
    
def diff(wrong, correct):
    """ diff two lists"""
    i = 0
    # equal parts from the beginning
    while  i < min(len(wrong),len(correct)) and correct[i] == wrong[i]:
        i += 1
    start = i
    # equal parts from the end
    i = len(correct) -1
    j = len(wrong) -1
    if  i >= start  and j >= start and correct[i] == wrong[j]:
        i -= 1
        j -= 1
        
    end_correct = i
    end_wrong   = j
    correct_word = correct[start:end_correct]
    wrong = wrong[:start] +['<marker>',] + wrong[start:end_wrong] +['</marker>',]+ wrong[end_wrong:]
    
    return correct_word, wrong
if __name__ == "__main__":
  main()

