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
  npmDepsHash = "sha256-YFrdhqJf7i8odV8ft3pVfj2x6VNHpjY/+h6b/2lVebQ=";
}
