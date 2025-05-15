{
  lib,
  maven,
  jdk21_headless,
  todoapp-frontend,
  doCheck ? true,
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

    # mvnHash = lib.fakeHash;
    mvnHash = "sha256-rUC/OfiFAn5TivCp9r/y6BPXMFAvV8P5g710URx+PIk=";
    mvnJdk = jdk21_headless;

    inherit doCheck;

    installPhase = ''
      mkdir -p $out/bin

      cp target/${name}.jar $out/

      substitute $src/nix-run.sh $out/bin/${pname} \
        --replace-fail @JAVA@ ${jdk21_headless} \
        --replace-fail @JAR@ $out/${name}.jar

      chmod +x $out/bin/${pname}
    '';
  }
