{
  lib,
  writeTextFile,
  fish,
  symlinkJoin,
  ...
}: let
  writeFishScriptBin = name: contents: writeTextFile {
    inherit name;
    text = ''
      #! ${fish}/bin/fish
      ${contents}
    '';
    executable = true;
    destination = "/bin/${name}";
  };
  scripts = lib.pipe (builtins.readDir ./.) [
    (lib.filterAttrs (n: _: !(lib.hasSuffix ".nix" n)))
    (lib.filterAttrs (_: v: v == "regular"))
    (builtins.mapAttrs (n: _: builtins.readFile "${./.}/${n}"))
    (lib.mapAttrsToList writeFishScriptBin)
  ];
in symlinkJoin {
  name = "utils";
  paths = scripts;
}
