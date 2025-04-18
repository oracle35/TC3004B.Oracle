{
  inputs = {
    mvn2nix.url = "github:fzakaria/mvn2nix";
    utils.url = "github:numtide/flake-utils";
    nixpkgs.url = "nixpkgs/nixos-unstable";
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
            oci-cli
            kubectl
            (writeShellScriptBin "kube_auth.sh" ''
              TOKEN_FILE=$PROJECT_ROOT/.kube/TOKEN
              if ! test -f "$TOKEN_FILE" || test $(( `date +%s` - `stat -L --format %Y $TOKEN_FILE` )) -gt 240; then
                umask 177
                oci --config-file $PROJECT_ROOT/.oci/config ce cluster generate-token --cluster-id "$1" --region "$2" > $TOKEN_FILE
              fi
              cat $TOKEN_FILE
            '')
            packages.utils
          ];
          #inputsFrom = [packages.todoapp];

          shellHook = ''
            export PROJECT_ROOT=$(pwd)
            export KUBECONFIG=$(pwd)/.kube/config
            export IMAGE_NAME=${packages.dockerImage.imageName}
          '';
        };
      };
    });
}
