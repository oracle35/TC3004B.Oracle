{
  lib,
  buildNpmPackage
}: buildNpmPackage rec {
  pname = "todolistapp-frontend";
  version = "0.1.0";

  src = ./.;

  installPhase = ''
    mkdir -p $out
    cp -r dist/* $out
  '';

  npmDepsHash = "sha256-+ii1oay3I7mxlkx6Ie2o5IsZT4lbsZoU9oLcz9EYRIY=";
}
