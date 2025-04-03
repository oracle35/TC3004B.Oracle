{
  lib,
  dockerTools,
  cacert,
  jdk11_headless,
  jre_minimal,
  todoapp,
  dockerTag,
  stdenvNoCC,
  makeWrapper,
  ...
}: let
  jre = jre_minimal.override {
    jdk = jdk11_headless;
    modules = [
      "java.base"
      "java.desktop"
      "java.logging"
      "java.management"
      "java.naming"
      "java.security.jgss"
      "java.instrument"
      "java.sql"
    ];
  };

  app = stdenvNoCC.mkDerivation {
    inherit (todoapp) pname version;
    nativeBuildInputs = [ makeWrapper ];
    buildCommand = ''
      mkdir -pv $out/share/java $out/bin
      cp ${todoappJar} $out/share/java/$pname.jar
      makeWrapper ${jre}/bin/java $out/bin/app --add-flags "-jar $out/share/java/$pname.jar" 
    '';
  };

  todoappJar = "${todoapp}/${todoapp.name}.jar";
in
  dockerTools.streamLayeredImage {
    name = "ghcr.io/435vic/todoapp";
    tag = dockerTag;

    contents = [
      cacert
      ./wallet
    ];

    extraCommands = ''
      install -dm 1777 tmp
    '';

    passthru = {
      inherit app;
    };

    config = {
      Cmd = [ "${app}/bin/app" ];
    };
  }
