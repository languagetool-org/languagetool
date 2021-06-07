# Compile & run LT from code

- Clone the repo.
- From root directory, run ./build.sh languagetool-standalone package -DskipTests to build standalone (similar to deployed docker https://github.com/Erikvl87/docker-languagetool).
- From languagetool-standalone/target/LanguageTool-5.3-SNAPSHOT/LanguageTool-5.3-SNAPSHOT (5.3-SNAPSHOT is the version) run ./start.sh to start the local LT.
- Spell check your text (e.g. http://localhost:8010/v2/check?language=no&text=forbrukslån)

# Run docker
- Clone the repo.
- Move to `languagetool-standalone`.
- Build docker image `docker build -t languagetool .`.
- Run the container `docker run -p 8010:8010 --name languagetool languagetool`.
- Spell check your text (e.g. http://localhost:8010/v2/check?language=no&text=forbrukslån)
