{
  lib,
  maven,
  jdk11_headless,
  todoapp-frontend,
  ...
}:
  maven.buildMavenPackage rec {
    pname = "MyTodoList";
    version = "0.0.1-SNAPSHOT";
    name = "${pname}-${version}";

    src = ./.;

    preBuild = ''
      ln -s ${todoapp-frontend} ./target/frontend
    '';

    mvnHash = lib.fakeHash;

    installPhase = ''
      mkdir -p $out/bin

      cp target/${name}.jar $out/
      cp -r $src/wallet $out/

      makeWrapper ${jdk11_headless}/bin/java $out/bin/${pname} \
        --add-flags "-jar $out/${name}.jar"
    '';
  }
