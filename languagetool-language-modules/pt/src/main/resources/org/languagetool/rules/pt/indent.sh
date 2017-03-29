echo 'Please wait...'

sed -ri 's/^[ \t]+(<\/?rulegroup)/  \1/' $@
sed -ri 's/^[ \t]+(<\/?(rule |rule>|!--))/    \1/' $@
sed -ri 's/^[ \t]+(<\/?(marker|suggestion|equivalence|and|or))/        \1/' $@
sed -ri 's/^[ \t]+(<\/?(antipattern|pattern|message|url|short|example|disambig|unification))/      \1/' $@
sed -ri 's/^[ \t]+(<\/?token)/          \1/' $@
sed -ri 's/^[ \t]+(<\/?exception)/            \1/' $@

echo $@' indented'
