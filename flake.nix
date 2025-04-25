{
  inputs = {
    utils.url = "github:numtide/flake-utils";
    nixpkgs.url = "nixpkgs/nixos-unstable";
  };

  outputs = {
    self,
    nixpkgs,
    utils,
    ...
  }:
    utils.lib.eachSystem utils.lib.defaultSystems (system: let
      pkgs = import nixpkgs {
        inherit system;
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

        todoapp = {
          type = "app";
          program = "${packages.todoapp-dev}/bin/${packages.todoapp-dev.pname}";
        };
      };

      devShells = {
        backend = pkgs.mkShell {
          packages = with nixpkgs.legacyPackages.${system}; [
            packages.nodePkgs.gh-actions-language-server
            jdt-language-server
            jdk21
            oci-cli
            sqlcl
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
            export COMPARTMENT_OCID="ocid1.compartment.oc1..aaaaaaaaqnxoiiosc4hv3in5uuugiykxbtsj4qk6d2eqxa6c42f257rpgydq"
            export DB_OCID="ocid1.autonomousdatabase.oc1.mx-queretaro-1.anyxeljrlzse2vyaamhnqpcdyerprtaauufu6svawp5mftssif7tv3prbztq"
            export TNS_ADMIN=$PROJECT_ROOT/wallet
            export KUBECONFIG=$(pwd)/.kube/config
            export IMAGE_NAME=${packages.dockerImage.imageName}
          '';
        };
      };
    });
}
