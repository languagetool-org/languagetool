#!/usr/bin/env python3
# coding: utf-8

# Program reads list of Serbian words with frequencies,
# making special file with wordlist frequencies
#

import argparse
import logging
import os
import sys

"""
Program creates wordlist file processing word list selected
from database. Format of input file is:

wordform   lemma   postag   frequency

Items are separated with <TAB> character.
"""

_args_, _logger_, _freqs_, _freqmap_ = None, None, list(), dict()
LOG_FORMAT = '%(asctime)-15s %(levelname)s %(message)s'

def parse_args():
    parser = argparse.ArgumentParser(description='Processes file containing Serbian word corpus.')
    parser.add_argument('-b', '--base', default=255, type=int)
    parser.add_argument('-d', '--debug',      action ='store_true', default=False)
    parser.add_argument('-i', '--input-file', default=None)
    parser.add_argument('-n', '--first-n-lines', default=0, type=int)
    parser.add_argument('-o', '--output-file', default=None)

    global _args_, _logger_
    _args_ = parser.parse_args()
    logging.basicConfig(format=LOG_FORMAT)
    _logger_ = logging.getLogger("makewl")
    if _args_.debug:
        _logger_.setLevel( logging.DEBUG )
    else:
        _logger_.setLevel( logging.INFO )
    _logger_.debug( "Command-line arguments: {}".format(_args_) )
    if not _args_.input_file:
        _logger_.error("Input file (-i) was not specified, aborting ...")
        sys.exit(1)
    if not os.path.exists(_args_.input_file):
        _logger_.error("Input file '{}' does not exist, aborting ...".format(_args_.input_file))
        sys.exit(1)
    if not _args_.output_file:
        _logger_.error("Output file (-o) was not specified, aborting ...")
        sys.exit(1)


# Go through input file, read word frequencies and prepare map file
# Map file will be used in dictionary creation process
def find_frequencies():
    global _freqs_
    cnt = 1
    matchcnt = 0
    _logger_.info("PASS 1: Started processing input file '{}', getting word frequencies ...".format(_args_.input_file))
    freq = list()

    with open(_args_.input_file) as f:
        for line in f:
            # Remove end of line
            line = line.strip()
            tokens = line.split('\t')
            if len(tokens) == 4:
                matchcnt += 1
                frequency = tokens[3]
                _logger_.debug('cnt={} frequency={}'.format(cnt, frequency))
                # Do not take punctuation signs
                if int(frequency) not in freq:
                    freq.append( int(frequency) )
            else:
                _logger_.warn("Unmatched line: {}".format(line))
            if cnt == _args_.first_n_lines > 0:
                break
            cnt += 1
        f.close()
    _logger_.info( "PASS 1: End processing input file '{}', matched {} lines.".format(_args_.input_file, matchcnt))
    _logger_.info( "PASS 1: Got {} different word frequencies.".format(len(freq)) )
    _freqs_ = sorted(freq)


# Maps word frequencies to numbers from 1 to 255
# With this algorithm we try equal distribution
def distribute_word_frequencies():
    global _freqmap_
    _logger_.info( "Frequencies: first {}, last {}.".format(_freqs_[0], _freqs_[-1]) )
    len_freq = len(_freqs_)
    bucket_size = len_freq // _args_.base + 1
    _logger_.debug( "Frequency list bucket size: {}".format(bucket_size) )
    cnt = 0

    for msb in range(0, _args_.base):
        for lsb in range(0, bucket_size):
            cnt += 1
            ind = msb * bucket_size + lsb
            if ind < len_freq:
                _freqmap_[ _freqs_[ ind ] ] = msb + 1
                _logger_.debug( 'msb={} lsb={} ind={}, {} => {}'.format(msb, lsb, ind, _freqs_[ ind ], msb+1))
            if cnt > _args_.first_n_lines > 0:
                break


# Parse input file
def parse_file():
    cnt = 1
    matchcnt = 0
    _logger_.info("PASS 2: Started processing input file '{}' ...".format(_args_.input_file))
    freqfile = open(_args_.output_file, "wb")

    with open(_args_.input_file) as f:
        for line in f:
            # Remove end of line
            line = line.strip()
            tokens = line.split('\t')
            if len(tokens) == 4:
                matchcnt += 1
                flexform = tokens[0]
                frequency = tokens[3]
                # Write to frequency file
                freqfile.write('<w f="{}" flags="">{}</w>\n'.format(_freqmap_[ int(frequency) ], flexform).encode('utf-8'))
            else:
                _logger_.warn("Unmatched line: {}".format(line))
            if cnt == _args_.first_n_lines > 0:
                break
            cnt += 1
        f.close()
    freqfile.close()
    _logger_.info("PASS 2: Finished processing input file '{}': total {} lines, {} matching lines.".format(
        _args_.input_file, cnt, matchcnt))


if __name__ == "__main__":
    parse_args()
    find_frequencies()
    distribute_word_frequencies()
    parse_file()
