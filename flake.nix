{
  inputs = {
    mvn2nix.url = "github:fzakaria/mvn2nix";
    utils.url = "github:numtide/flake-utils";
    nixpkgs.url = "nixpkgs/1e5b653dff12029333a6546c11e108ede13052eb";
  };

  outputs = {
    nixpkgs,
    mvn2nix,
    utils,
    ...
  }: let
    overlay = final: prev: {
      myTodoListApp = final.callPackage ./MtdrSpring/backend/package.nix {};
      myTodoListApp-frontend = final.callPackage ./MtdrSpring/front/package.nix {};
    };
  in utils.lib.eachSystem utils.lib.defaultSystems (system: rec {
    legacyPackages = import nixpkgs {
      inherit system;
      overlays = [ mvn2nix.overlay overlay ];
    };
    packages = {
      inherit (legacyPackages) myTodoListApp;
    };
    defaultPackage = legacyPackages.myTodoListApp;
  }); 
}
