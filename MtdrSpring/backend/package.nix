{
  lib,
  stdenv,
  buildMavenRepositoryFromLockFile,
  makeWrapper,
  maven,
  jdk11_headless,
  todoapp-frontend,
  ...
}: let
  mavenRepository = buildMavenRepositoryFromLockFile {file = ./mvn2nix-lock.json;};
in
  stdenv.mkDerivation rec {
    pname = "MyTodoList";
    version = "0.0.1-SNAPSHOT";
    name = "${pname}-${version}";

    src = ./.;

    nativeBuildInputs = [jdk11_headless maven makeWrapper];

    buildPhase = ''
      mkdir -p target
      ln -s ${todoapp-frontend} ./target/frontend
      mvn package --offline -Dmaven.repo.local=${mavenRepository}
    '';

    passthru = {
      inherit mavenRepository;
    };

    installPhase = ''
      mkdir -p $out/bin

      ln -s ${mavenRepository} $out/lib

      cp target/${name}.jar $out/
      cp -r $src/wallet $out/

      substitute $src/nix-run.sh $out/bin/${pname} \
        --replace-fail @JAVA@ ${jdk11_headless} \
        --replace-fail @JAR@ $out/${name}.jar

      chmod +x $out/bin/${pname}
    '';
  }
