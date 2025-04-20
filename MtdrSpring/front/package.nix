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
  npmDepsHash = "sha256-Svu9H3j8VUUoawI7VZhU5sk23THwuovZIsv/bErdStY=";
}
