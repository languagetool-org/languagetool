echo 'Please wait...'

# only indenting replacements active
# lines commented-out do other types of clean-up/formatting
# uncomment if approved
#
# sed -ri 's/([^ ])(<exception)/\1\n            \2/' $@

sed -ri 's/^[ \t]*(<\/?category)/    \1/' $@
sed -ri 's/^[ \t]*(<\/?(rulegroup|!DOCTYPE|phrases|unification))/        \1/' $@
sed -ri 's/^[ \t]*(<\/?(rule[ >]|!ENTITY|phrase[ >]|equivalence))/            \1/' $@
# ^| !--| comments may have custom positioning
sed -ri 's/^[ \t]*(<\/?(marker|suggestion|and|or|wd))/                \1/' $@
sed -ri 's/^[ \t]*(<\/?(antipattern|pattern|regexp|filter|message|url|short|example|disambig|includephrases))/                \1/' $@
sed -ri 's/^[ \t]*(<\/?(token|unify|phraseref))/                    \1/' $@
sed -ri 's/^[ \t]*(<\/?(exception|feature))/                        \1/' $@

# sed -ri 's/(<(token|exception|suggestion|match|disambig|feature|phraseref|wd)[^>]*?)><\/\2>/\1\/>/' $@
# sed -ri 's/[ \t]+\r?$/\r/' $@
# sed -ri 's/" >/">/' $@
# sed -ri 's/ \/>/\/>/' $@
# sed -ri 's/\.\.\.<\/example>/…<\/example>/' $@
# sed -ri 's/<example>\.\.\./<example>…/' $@

echo $@' indented'
