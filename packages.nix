{
  pkgs,
  self,
}: rec {
  utils = pkgs.callPackage ./utils/package.nix {};
  todoapp-frontend = pkgs.callPackage ./MtdrSpring/front/package.nix {};
  todoapp-frontend-dev = pkgs.callPackage ./MtdrSpring/front/package.nix { doCheck = false; }; 
  todoapp = pkgs.callPackage ./MtdrSpring/backend/package.nix { inherit todoapp-frontend; };
  todoapp-dev = pkgs.callPackage ./MtdrSpring/backend/package.nix {
    todoapp-frontend = todoapp-frontend-dev;
    doCheck = false;
  };
  nodePkgs = import ./globalNodeEnv/default.nix {
    system = pkgs.system;
    pkgs = pkgs;
    nodejs = pkgs.nodejs_22;
  };

  dockerImage = pkgs.callPackage ./MtdrSpring/backend/docker.nix {
    inherit todoapp;
    dockerTag = self.rev or self.dirtyRev or self.lastModified;
  };

  claude-code =
    nodePkgs."@anthropic-ai/claude-code"
    // {
      meta.mainProgram = "claude";
    };
}
