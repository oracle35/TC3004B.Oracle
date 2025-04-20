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

  npmDepsHash = lib.fakeHash;
  # npmDepsHash = "sha256-dVXl6xj/FNKR2XDoKmy0ggkdTlPG0RIiQz2Z2p/e3j8=";
}
