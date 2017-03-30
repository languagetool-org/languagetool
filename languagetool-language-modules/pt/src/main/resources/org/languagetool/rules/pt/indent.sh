echo 'Please wait...'

sed -ri 's/^[ \t]*(<\/?category)/ \1/' $@
sed -ri 's/^[ \t]*(<\/?(rulegroup|!DOCTYPE))/  \1/' $@
sed -ri 's/^[ \t]*(<\/?(rule |rule>|!--|!ENTITY))/    \1/' $@
sed -ri 's/^[ \t]*(<\/?(marker|suggestion|equivalence|and|or))/        \1/' $@
sed -ri 's/^[ \t]*(<\/?(antipattern|pattern|message|url|short|example|disambig|unification))/      \1/' $@
sed -ri 's/^[ \t]*(<\/?(token|unify))/          \1/' $@
sed -ri 's/^[ \t]*(<\/?(exception|feature))/            \1/' $@

echo $@' indented'
