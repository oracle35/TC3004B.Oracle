{
  lib,
  buildNpmPackage,
  doCheck ? true,
}:
buildNpmPackage rec {
  pname = "todolistapp-frontend";
  version = "0.1.0";

  src = ./.;

  installPhase = ''
    mkdir -p $out
    cp -r dist/* $out
  '';

  inherit doCheck;
  checkPhase = ''
    npm run lint
    npm run test
  '';

  # npmDepsHash = lib.fakeHash;
  npmDepsHash = "sha256-jACdorwitANMLDxLexCTkX475qxiMu0ylXJU36/wlh8=";
}
