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
      mkdir -p target
      ln -s ${todoapp-frontend} ./target/frontend
    '';

    mvnHash = lib.fakeHash;

    installPhase = ''
      mkdir -p $out/bin

      cp target/${name}.jar $out/
      cp -r $src/wallet $out/

      substitute $src/nix-run.sh $out/bin/${pname} \
        --replace-fail @JAVA@ ${jdk11_headless} \
        --replace-fail @JAR@ $out/${name}.jar

      chmod +x $out/bin/${pname}
    '';
  }
