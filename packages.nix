{
  pkgs,
  self,
}: rec {
  todoapp-frontend = pkgs.callPackage ./MtdrSpring/front/package.nix {};
  todoapp = pkgs.callPackage ./MtdrSpring/backend/package.nix {inherit todoapp-frontend;};
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
