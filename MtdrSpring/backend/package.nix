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
  mavenRepository = buildMavenRepositoryFromLockFile { file = ./mvn2nix-lock.json; };
in stdenv.mkDerivation rec {
  pname = "MyTodoList";
  version = "0.0.1-SNAPSHOT";
  name = "${pname}-${version}";

  src = ./.;

  nativeBuildInputs = [ jdk11_headless maven makeWrapper ];

  patchPhase = ''
    substituteInPlace ./src/main/resources/application.properties \
      --replace-fail "@walletPath@" "${src}/wallet"
  '';

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

    cp target/${name}.jar $out/
    cp -r $src/wallet $out/

    makeWrapper ${jdk11_headless}/bin/java $out/bin/${pname} \
      --add-flags "-jar $out/${name}.jar"
  '';
}
