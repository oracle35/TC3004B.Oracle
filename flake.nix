{
  inputs = {
    mvn2nix.url = "github:fzakaria/mvn2nix";
    utils.url = "github:numtide/flake-utils";
    nixpkgs.url = "nixpkgs/1e5b653dff12029333a6546c11e108ede13052eb";
  };

  outputs = {
    self,
    nixpkgs,
    mvn2nix,
    utils,
    ...
  }:
    utils.lib.eachSystem utils.lib.defaultSystems (system: let
      pkgs = import nixpkgs {
        inherit system;
        overlays = [mvn2nix.overlay];
      };

      # Import the packages
      packages = import ./packages.nix {
        inherit pkgs self;
      };
    in {
      inherit packages;

      apps = {
        docker = {
          type = "app";
          program = "${packages.dockerImage}";
        };
      };

      devShells = {
        backend = pkgs.mkShell {
          packages = with nixpkgs.legacyPackages.${system}; [
            packages.nodePkgs.gh-actions-language-server
            jdt-language-server
            jdk21
          ];
          #inputsFrom = [packages.todoapp];
        };
      };

      defaultPackage = packages.todoapp;
    });
}
