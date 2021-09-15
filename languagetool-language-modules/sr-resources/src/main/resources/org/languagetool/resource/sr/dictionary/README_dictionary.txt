How to get Serbian corpus and frequency files
=============================================

We assume that input file for creating Serbian dictionaries is one text file with format:

flexform    lemma   MDS_tag frequency   ...

In order to get Serbian word corpus file one must do:

1. Run Python program "lex2lt.py" with parameters:
    ./lex2lt.py -i <input_file> -b <base_output_pos_dir> -r lex

After this step we have hierarchy for all words in <base_output_pos_dir> :

    <base_output_pos_dir>/a/a-words.txt
    <base_output_pos_dir>/a/a-names.txt
    <base_output_pos_dir>/be/be-words.txt
    <base_output_pos_dir>/be/be-names.txt
    ...
    <base_output_pos_dir>/ze/ze-names.txt
    <base_output_pos_dir>/ze/ze-words.txt

We can optionally create files

    <some_directory>/static/<letter>/<letter>-sed-cmds.txt

that will contain commands to replace words( e.g. "Аацхен" with "Ахен").

2. Run Python script "pos2lt.py" with parameters:
    ./pos2lt.py -i <path_to_input_LEX_file> -o <path_to_output_directory>

3. Run shell script "tag2corp.sh"
    ./tag2corp.sh

That will replace word in word lists.