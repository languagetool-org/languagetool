echo 'Please wait...'

sed -ri 's/^[ \t]*(<\/?category)/ \1/' $@
sed -ri 's/^[ \t]*(<\/?(rulegroup|!DOCTYPE))/  \1/' $@
sed -ri 's/^[ \t]*(<\/?(rule |rule>|!--|!ENTITY))/    \1/' $@
sed -ri 's/^[ \t]*(<\/?(marker|suggestion|equivalence|and|or|wd))/        \1/' $@
sed -ri 's/^[ \t]*(<\/?(antipattern|pattern|regexp|filter|message|url|short|example|disambig|unification))/      \1/' $@
sed -ri 's/^[ \t]*(<\/?(token|unify))/          \1/' $@
sed -ri 's/^[ \t]*(<\/?(exception|feature))/            \1/' $@

sed -ri 's/(<(token|exception|suggestion|match|disambig|feature|wd)[^>]*?)><\/\2>/\1\/>/' $@
sed -ri 's/[ \t]+\r?$/\r/' $@
sed -ri 's/" >/">/' $@
sed -ri 's/ \/>/\/>/' $@

echo $@' indented'
