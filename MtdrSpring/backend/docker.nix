{
  lib,
  dockerTools,
  cacert,
  iana-etc,
  jdk21_headless,
  jre_minimal,
  todoapp,
  dockerTag,
  stdenvNoCC,
  makeWrapper,
  ...
}: let
  jre = jre_minimal.override {
    jdk = jdk21_headless;
    modules = [
      "java.base"
      "java.desktop"
      "java.logging"
      "java.management"
      "java.naming"
      "jdk.crypto.ec" # telegram has an elliptic curve SSL certificate
      "java.net.http"
      "java.security.jgss"
      "java.instrument"
      "java.sql"
      "jdk.unsupported"
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
    name = "todoapp";
    tag = dockerTag;

    contents = [
      cacert
      iana-etc
    ];

    extraCommands = ''
      install -dm 1777 tmp
    '';

    passthru = {
      inherit app jre;
    };

    config = {
      Env = [
        "IS_CONTAINER=1"
      ];
      Cmd = [ "${app}/bin/app" ];
    };
  }
