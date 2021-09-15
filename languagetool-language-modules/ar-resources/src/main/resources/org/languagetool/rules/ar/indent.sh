echo 'Please wait...'

gsed -ri 's/([^ ])(<exception)/\1\n            \2/' $@

gsed -ri 's/^[ \t]*(<\/?category)/ \1/' $@
gsed -ri 's/^[ \t]*(<\/?(rulegroup|!DOCTYPE|phrases|unification))/  \1/' $@
gsed -ri 's/^[ \t]*(<\/?(rule[ >]|!--|!ENTITY|phrase[ >]|equivalence))/    \1/' $@
gsed -ri 's/^[ \t]*(<\/?(marker|suggestion|and|or|wd))/        \1/' $@
gsed -ri 's/^[ \t]*(<\/?(antipattern|pattern|regexp|filter|message|url|short|example|disambig|includephrases))/      \1/' $@
gsed -ri 's/^[ \t]*(<\/?(token|unify|phraseref))/          \1/' $@
gsed -ri 's/^[ \t]*(<\/?(exception|feature))/            \1/' $@

gsed -ri 's/(<(token|exception|suggestion|match|disambig|feature|phraseref|wd)[^>]*?)><\/\2>/\1\/>/' $@
gsed -ri 's/[ \t]+\r?$/\r/' $@
gsed -ri 's/" >/">/' $@
gsed -ri 's/ \/>/\/>/' $@
gsed -ri 's/\.\.\.<\/example>/…<\/example>/' $@
gsed -ri 's/<example>\.\.\./<example>…/' $@

echo $@' indented'
