{
  lib,
  buildNpmPackage,
}:
buildNpmPackage rec {
  pname = "todolistapp-frontend";
  version = "0.1.0";

  src = ./.;

  installPhase = ''
    mkdir -p $out
    cp -r dist/* $out
  '';

  npmDepsHash = "sha256-GsI1ykfayuixWNZK1MLYegcged7mtCgI0ru0WuVyB/I=";
}
